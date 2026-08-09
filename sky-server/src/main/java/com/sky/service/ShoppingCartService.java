package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 添加购物车
     */
    List<ShoppingCart> list();

    /**
     * 添加购物车
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 删除购物车
     */
    void sub(ShoppingCartDTO shoppingCartDTO);

    /**
     * 清空购物车
     */
    void clean();
}
