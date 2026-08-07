package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.annotation.AutoFill;
import com.sky.entity.Category;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Override
    @AutoFill(OperationType.INSERT)
    int insert(Category category);

    @Override
    @AutoFill(OperationType.UPDATE)
    int updateById(@Param("et") Category category);
}
