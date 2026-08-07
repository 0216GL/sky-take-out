package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;

public interface SetmealService {
    /**
     * 套餐分页查询
     */
    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     */
    void save(SetmealDTO setmealDTO);
}
