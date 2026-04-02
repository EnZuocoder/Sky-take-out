package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SetmealDishMapper {

    Integer countByDishIds(Long[] dishIds);
}

