package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
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

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 分类分页查询
     * @return
     */
    @Override
    public Object page(CategoryPageQueryDTO categoryPageQueryDTO) {
        String name = categoryPageQueryDTO.getName();
        Integer type = categoryPageQueryDTO.getType();
        Integer pageSize = categoryPageQueryDTO.getPageSize();
        Integer page = categoryPageQueryDTO.getPage();
        Long id = categoryPageQueryDTO.getCategoryId();

        Page<Category> pageParam = new Page<>(page, pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Category::getName, name);
        queryWrapper.eq(type != null, Category::getType, type);
        queryWrapper.eq(id != null, Category::getId, id);
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
        Integer type = category.getType();
        if (type == 1) {
            Long count = dishMapper.selectCount(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
            if (count > 0) {
                throw new DeletionNotAllowedException("当前分类关联了菜品，不能删除");
            }
        }else{
            Long count = setmealMapper.selectCount(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, id));
            if (count > 0) {
                throw new DeletionNotAllowedException("当前分类关联了套餐，不能删除");
            }
        }
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
