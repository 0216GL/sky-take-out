package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    @Override
    @AutoFill(OperationType.INSERT)
    int insert(Dish dish);

    @Override
    @AutoFill(OperationType.UPDATE)
    int updateById(@Param("et") Dish dish);
}
