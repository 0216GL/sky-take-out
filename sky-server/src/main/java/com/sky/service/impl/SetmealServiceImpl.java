package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.annotations.Until;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 分页查询
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        Page<Setmeal> page = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(setmealPageQueryDTO.getName() != null,Setmeal::getName, setmealPageQueryDTO.getName())
                .eq(setmealPageQueryDTO.getCategoryId() != null,Setmeal::getCategoryId, setmealPageQueryDTO.getCategoryId())
                .eq(setmealPageQueryDTO.getStatus() != null,Setmeal::getStatus, setmealPageQueryDTO.getStatus());

        setmealMapper.selectPage(page, queryWrapper);

        return new PageResult(page.getTotal(), page.getRecords());
    }

    /**
     * 新增套餐
     */
    @Override
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
//        TODO
    }


}
