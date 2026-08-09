package com.sky.controller.user;

import com.sky.context.UserContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("user/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;
    /**
     * 查看购物车
     */
    @RequestMapping("list")
    public Result<List<ShoppingCart>> list(){
        List<ShoppingCart> shoppingCart = shoppingCartService.list();
        return Result.success(shoppingCart);
    }

    /**
     * 添加购物车
     */
    @RequestMapping("add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.add(shoppingCartDTO);
        log.info("添加购物车：{}", shoppingCartDTO);
        return Result.success();
    }

    /**
     * 删除购物车
     */
    @RequestMapping("sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 清空购物车
     */
    @RequestMapping("clean")
    public Result clean(){
        shoppingCartService.clean();
        return Result.success();
    }
}
