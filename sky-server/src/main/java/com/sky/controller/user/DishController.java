package com.sky.controller.user;

import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController("userDishController")
@Slf4j
@RequestMapping("/user/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 菜品分页查询
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        return Result.success(dishService.page(dishPageQueryDTO));
    }



    /**
     * 获取指定分类下的菜品
     */
    @GetMapping("/list")
    public Result<List<DishVO>> list(Long categoryId) {

//        构造redis中的key
        String key = "dish_" + categoryId;

//        查询redis中缓存的菜品数据
        List<DishVO>  list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if(list != null && list.size() > 0){
            return Result.success(list);
        }

//        如果没有查询到，则查询数据库
        list = dishService.list(categoryId);
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }


}
