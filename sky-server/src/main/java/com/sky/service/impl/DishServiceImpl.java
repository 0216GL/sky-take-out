package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 菜品查询
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        Page<Dish> pageParam = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(dishPageQueryDTO.getName() != null, Dish::getName, dishPageQueryDTO.getName())
                .eq(dishPageQueryDTO.getCategoryId() != null, Dish::getCategoryId, dishPageQueryDTO.getCategoryId())
                .eq(dishPageQueryDTO.getStatus() != null, Dish::getStatus, dishPageQueryDTO.getStatus());

        dishMapper.selectPage(pageParam, queryWrapper);

        List<Dish> dishes = pageParam.getRecords();
        List<DishVO> dishVOList = dishes.stream().map(dish -> {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            Category category = categoryMapper.selectById(dish.getCategoryId());
            if (category != null) {
                dishVO.setCategoryName(category.getName());
            }
            return dishVO;
        }).collect(Collectors.toList());

        return new PageResult(pageParam.getTotal(), dishVOList);
    }

    /**
     * 启用禁用菜品
     */
    @Override
    public void startOrStop(Integer statu, Long id) {
        Dish dish = Dish.builder()
                .status(statu)
                .id(id)
                .build();
        dishMapper.updateById(dish);
    }

    /**
     * 根据id查询菜品
     */
    @Override
    public DishDTO ListById(Long id) {
        DishDTO dishDTO = new DishDTO();
        Dish dish = dishMapper.selectById(id);
        BeanUtils.copyProperties(dish, dishDTO);
        return dishDTO;
    }

    /**
     * 修改菜品
     */
    @Override
    public Object update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateById(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dish.getId());
        }

        dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>().eq(DishFlavor::getDishId, dish.getId()));
        if(flavors != null && flavors.size() > 0){
            for(DishFlavor flavor : flavors){
                dishFlavorMapper.insert(flavor);
            }
        }
        return null;
    }

    /**
     * 新增菜品
     */
    @Override
    public Result save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dish.getId());
        }
        if(flavors != null && flavors.size() > 0){
            for(DishFlavor flavor : flavors){
                dishFlavorMapper.insert(flavor);
            }
        }
        return Result.success();
    }

    /**
     * 删除菜品
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {

        List<Dish> dishes = dishMapper.selectBatchIds(ids);
        for (Dish dish : dishes){
            if(dish.getStatus() == 1){
                throw new RuntimeException("当前有菜品正在起售中，不能删除");
            }
        }

        Long count =setmealDishMapper.selectCount(new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getDishId, ids));
        if(count > 0){
            throw new RuntimeException("当前有菜品在套餐中，不能删除");
        }

        try {
            dishFlavorMapper.delete(new LambdaQueryWrapper<DishFlavor>().in(DishFlavor::getDishId, ids));
        } catch (Exception e) {
            throw new RuntimeException("删除菜品口味失败");
        }

        dishMapper.deleteBatchIds(ids);


    }

    /**
     * 获取菜品列表
     */
    @Override
    public List<DishVO> list(Long categoryId) {
        List<DishVO> dishVOList = new ArrayList<>();
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, categoryId));
        for(Dish dish : dishes){
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }

}
