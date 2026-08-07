package com.sky.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.utils.AliOssUtil;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

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

        List<SetmealVO> setmealVOList = page.getRecords().stream().map(setmeal -> {
            SetmealVO setmealVO = new SetmealVO();
            BeanUtils.copyProperties(setmeal, setmealVO);
            if (setmeal.getCategoryId() != null) {
                Category category = categoryMapper.selectById(setmeal.getCategoryId());
                if (category != null) {
                    setmealVO.setCategoryName(category.getName());
                }
            }
            return setmealVO;
        }).collect(Collectors.toList());

        return new PageResult(page.getTotal(), setmealVOList);
    }

    /**
     * 新增套餐
     */
    @Override
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        setmealDTO.getSetmealDishes().forEach(dish -> dish.setSetmealId(setmeal.getId()));
        for (SetmealDish dish : setmealDTO.getSetmealDishes()){
            setmealDishMapper.insert(dish);
        }
    }

    /**
     * 根据id查询套餐及关联菜品
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        if (setmeal == null) {
            throw new BaseException("套餐不存在");
        }
        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(
                new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, id));
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        if (setmeal.getCategoryId() != null) {
            Category category = categoryMapper.selectById(setmeal.getCategoryId());
            if (category != null) {
                setmealVO.setCategoryName(category.getName());
            }
        }
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }


    /**
     * 修改套餐
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmeal.getId());
        }
        setmealMapper.updateById(setmeal);
        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>().eq(SetmealDish::getSetmealId, setmeal.getId()));
        for (SetmealDish setmealDish : setmealDishes){
            setmealDishMapper.insert(setmealDish);
        }

    }

    /**
     * 删除套餐
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        List<Setmeal> setmeals = setmealMapper.selectBatchIds(ids);
        for (Setmeal setmeal : setmeals) {
            if (setmeal == null) {
                throw new BaseException("套餐不存在");
            }
            if (Integer.valueOf(1).equals(setmeal.getStatus())) {
                throw new BaseException("套餐正在售卖中，不能删除");
            }
        }

        // 删除套餐与菜品的关联关系
        setmealDishMapper.delete(new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getSetmealId, ids));

        // 删除套餐记录
        setmealMapper.deleteBatchIds(ids);

        // 删除OSS上的图片
        for (Setmeal setmeal : setmeals) {
            if (setmeal.getImage() != null && !setmeal.getImage().isEmpty()) {
                String objectName = setmeal.getImage().substring(setmeal.getImage().lastIndexOf("/") + 1);
                aliOssUtil.delete(objectName);
            }
        }
    }

    /**
     * 批量起售停售
     */
    @Override
    public void startOrStop(Integer status, Long id) {

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.updateById(setmeal);

    }
}
