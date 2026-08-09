package com.sky.controller.admin;

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

@RestController("adminSermealController")
@Slf4j
@RequestMapping("/admin/setmeal")
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
     * 新增套餐
     */
    @PostMapping
    public Result save(@RequestBody SetmealDTO setmealDTO){
        setmealService.save(setmealDTO);
        return Result.success();
    }

    /**
     * 根据id查询套餐
     */
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id){
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     */
    @PutMapping
    public Result update(@RequestBody SetmealDTO setmealDTO){
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 批量删除套餐
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        setmealService.delete(ids);
        return Result.success();
    }

    /**
     * 套餐起售、停售
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id){
        setmealService.startOrStop(status, id);
        return Result.success();
    }
}
