package com.sky.controller.user;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetmealController")
@Slf4j
@RequestMapping("/user/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

     /**
     分页查询
      */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        PageResult pageResult = setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }


    /**
     * 获取套餐信息
     */
    @GetMapping("/list")
    public Result<List<SetmealVO>> list(Long categoryId){
        List<SetmealVO> list = setmealService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 根据id查询套餐
     */
//    @GetMapping("/dish/{id}")
//    public Result<SetmealVO> getById(@PathVariable Long id){
//        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
//        return Result.success(setmealVO);
//    }
}
