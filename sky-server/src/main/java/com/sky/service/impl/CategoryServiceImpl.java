package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.CategoryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 分类分页查询
     * @param page
     * @param pageSize
     * @param name
     * @param type
     * @return
     */
    @Override
    public PageResult page(Integer page, Integer pageSize, String name, String type) {
        Page<Category> pageParam = new Page<>(page, pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Category::getName, name);
        queryWrapper.eq(type != null, Category::getType, type);
        queryWrapper.orderByDesc(Category::getSort);

        categoryMapper.selectPage(pageParam, queryWrapper);

        return new PageResult(pageParam.getTotal(), pageParam.getRecords());
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.updateById(category);
    }

    /**
     * 新增分类
     * @param categoryDTO
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(1);
        categoryMapper.insert(category);
    }

    /**
     * 删除分类
     * @param id
     */
    @Override
    public void delete(Long id) {
        Category category = categoryMapper.selectById(id);
        if (!Integer.valueOf(0).equals(category.getStatus())) {
            throw new DeletionNotAllowedException("当前分类为启用状态，不能删除");
        }
//        TODO
        categoryMapper.deleteById(id);
    }

    /**
     * 启用禁用分类
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = new Category();
        category.setStatus(status);
        category.setId(id);
        categoryMapper.updateById(category);
    }

    /**
     * 查询分类
     */
    @Override
    public List<Category> list(Integer type) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(type != null, Category::getType, type);
        queryWrapper.orderByAsc(Category::getSort);
        List<Category> list = categoryMapper.selectList(queryWrapper);
        return list;
    }


}
