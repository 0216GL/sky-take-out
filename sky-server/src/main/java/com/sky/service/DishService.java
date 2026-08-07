package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 菜品分页查询
     */
    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 启用禁用菜品
     */
    void startOrStop(Integer statu, Long id);

    /**
     * 根据id查询菜品，回显
     */
    DishDTO ListById(Long id);

    /**
     * 修改菜品
     */
    Object update(DishDTO dishDTO);

    /**
     * 新增菜品
     */
    Object save(DishDTO dishDTO);

    /**
     * 删除菜品
     */
    void delete(List<Long> ids);

    /**
     * 根据分类id查询菜品
     */
    List<DishVO> list(Long categoryId);
}
