package com.sky.controller.admin;


import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.awt.*;

/**
 * 分类管理
 */
@RestController
@Slf4j
@RequestMapping("admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     *  分类分页查询
     */
    @GetMapping("page")
    public Result page(CategoryPageQueryDTO categoryPageQueryDTO){
        return Result.success(categoryService.page(categoryPageQueryDTO));
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    public Result update(@RequestBody CategoryDTO categoryDTO){
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody CategoryDTO categoryDTO){
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping
    public Result delete(@RequestParam Long id){
        try {
            categoryService.delete(id);
        } catch (Exception e) {
            if(e.getMessage() .equals("当前分类关联了套餐，不能删除"))
            return Result.error("当前分类关联了套餐，不能删除");
            else if(e.getMessage() .equals("当前分类关联了菜品，不能删除"))
            return Result.error("当前分类关联了菜品，不能删除");
            else if(e.getMessage() .equals("当前分类为启用状态，不能删除"))
            return Result.error("当前分类为启用状态，不能删除");
            return Result.error("未知错误");
        }
        return Result.success();
    }

    /**
     * 启用禁用分类
     */
    @PostMapping ("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id){
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 查询分类
     */
    @GetMapping("list")
    public Result list(Integer type){
        return Result.success(categoryService.list(type));
    }

}
