package com.sky.controller.user;


import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取店铺的营业状态
     */
    @GetMapping("/status")
    public Result<Integer> get(){
        Integer status = (Integer) redisTemplate.opsForValue().get("shop:status");
        return Result.success(status);
    }

    /**
     * 设置店铺的营业状态
     *
     */
    @RequestMapping("/set")
    public Result set(){
        redisTemplate.opsForValue().set("shop:status",1);
        return Result.success();
    }

    /**
     * 营业状态设置
     */
    @RequestMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        redisTemplate.opsForValue().set("shop:status",status);
        return Result.success();
    }
}
