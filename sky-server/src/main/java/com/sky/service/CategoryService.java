package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;

import java.util.List;

public interface CategoryService {
    /**
     * 分类分页查询
     */
    Object page(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 修改分类
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 新增分类
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 删除分类
     */
    void delete(Long id);

    /**
     * 启用禁用分类
     */
    void startOrStop(Integer status, Long id);

    /**
     * 查询分类
     */
    List<Category> list(Integer type);

}
