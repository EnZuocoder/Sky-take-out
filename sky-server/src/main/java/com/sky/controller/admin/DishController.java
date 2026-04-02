package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    //新增菜品
    @PostMapping
    public Result<Object> addDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
         dishService.addDish(dishDTO);
        return Result.success();
    }
    //分页查询菜品
    @GetMapping("/page")
    public Result<PageResult>queryDishByPage(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询菜品：{}", dishPageQueryDTO);
        return Result.success(dishService.queryDishByPage(dishPageQueryDTO));
    }
    @DeleteMapping
    public Result deleteDishes(Long[] ids){
        log.info("删除菜品：{}", ids);
        dishService.deleteDishes(ids);
        return Result.success();
    }
    //根据id查询菜品信息和口味信息
    @GetMapping("/{id}")
    public Result<DishVO> getDishById(@PathVariable Long id){
            log.info("根据id查询菜品信息和口味信息：{}", id);
            return Result.success(dishService.getDishById(id));
    }
    //修改菜品
    @PutMapping
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}", dishDTO);
        dishService.updateDish(dishDTO);
        return Result.success();
    }
}
