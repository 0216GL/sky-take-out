package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;

import java.util.List;

public interface CategoryService {
    /**
     * 分类分页查询
     * @param page
     * @param pageSize
     * @param name
     * @param type
     * @return
     */
    Object page(Integer page, Integer pageSize, String name, String type);

    /**
     * 修改分类
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 删除分类
     * @param id
     */
    void delete(Long id);

    /**
     * 启用禁用分类
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 查询分类
     */
    List<Category> list(Integer type);

}
