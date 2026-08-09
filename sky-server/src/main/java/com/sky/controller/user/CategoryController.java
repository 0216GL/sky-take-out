package com.sky.controller.user;


import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理
 */
@RestController("UserCategoryController")
@Slf4j
@RequestMapping("user/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询分类
     */
    @GetMapping("list")
    public Result list(Integer type){
        return Result.success(categoryService.list(type));
    }

}
