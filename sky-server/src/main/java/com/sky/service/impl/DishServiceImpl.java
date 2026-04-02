package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.utils.AliyunOssOperator;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private AliyunOssOperator aliyunOssOperator;

    @Override
    @Transactional // 开启事务,保证新增菜品和口味的原子性
    public void addDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 1.新增菜品
        dishMapper.insert(dish);
        Long dishId = dish.getId(); // 获取新增菜品的id

        // 2.新增口味
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId); // 设置口味的菜品id
        }
        dishFlavorMapper.insertBatch(flavors);
    }

    @Override
    public PageResult queryDishByPage(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<Dish> res = dishMapper.getDishByPage(dishPageQueryDTO);
        return new PageResult(res.getTotal(), res.getResult());
    }

    // 起售中的菜品不能删除
    // 被套餐关联的菜品不能删除
    // 删除菜品后关联口味数据也要删掉
    @Override
    @Transactional
    public void deleteDishes(Long[] ids) {
        // 1.批量查询菜品，任一处于起售状态都不允许本次删除
        List<Dish> dishList = dishMapper.listByIds(ids);
        for (Dish dish : dishList) {
            if (StatusConstant.ENABLE.equals(dish.getStatus())) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 2.校验是否被套餐关联，若有关联则整批不删除
        Integer count = setmealDishMapper.countByDishIds(ids);
        if (count != null && count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 3.删除 OSS 图片
        for (Dish dish : dishList) {
            aliyunOssOperator.deleteByUrl(dish.getImage());
        }

        // 4.先删口味，再删菜品
        dishFlavorMapper.deleteByDishIds(ids);
        dishMapper.deleteByIds(ids);
    }

    @Override
    public DishVO getDishById(Long id) {

        // 1.查询菜品基本信息
        Dish dish = dishMapper.getById(id);
        if (dish == null) {
            return null;
        }
        // 2.查询菜品口味信息
        List<DishFlavor> flavors = dishFlavorMapper.listByDishId(id);
        // 3.封装成VO对象返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    public void updateDish(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        Long dishId = dish.getId();

        // 1.更新菜品基本信息
        dishMapper.update(dish);


        if (flavors != null && !flavors.isEmpty()) {
            // 2.删除原有口味数据
            dishFlavorMapper.deleteByDishId(dishId);
            // 3.新增口味数据
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
