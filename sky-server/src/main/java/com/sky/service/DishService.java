package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

public interface DishService {
     void addDish(DishDTO dishDTO);

    PageResult queryDishByPage(DishPageQueryDTO dishPageQueryDTO);

    void deleteDishes(Long[] ids);

    DishVO getDishById(Long id);

    void updateDish(DishDTO dishDTO);
}
