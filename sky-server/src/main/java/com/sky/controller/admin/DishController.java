package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        return Result.success(dishService.page(dishPageQueryDTO));
    }

    /**
     * 启用禁用菜品
     */
    @PostMapping("status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        return Result.success( "状态修改成功");
    }

    /**
     * 根据id查询菜品
     */
    @GetMapping("/{id}")
    public Result<DishDTO> getById(@PathVariable Long id) {
        return Result.success(dishService.ListById(id));
    }


    /**
     * 修改菜品
     */
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO) {
        return Result.success(dishService.update(dishDTO));
    }

    /**
     * 新增菜品
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        return Result.success(dishService.save(dishDTO));
    }
}
