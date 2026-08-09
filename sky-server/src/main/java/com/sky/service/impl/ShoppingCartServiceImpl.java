package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.context.UserContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 查询购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        Long userId = UserContext.getCurrentId();
        if (userId == null) {
            throw new ShoppingCartBusinessException("用户未登录");
        }
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId)
                .orderByAsc(ShoppingCart::getCreateTime)
                .orderByAsc(ShoppingCart::getId);

        return shoppingCartMapper.selectList(queryWrapper);
    }
    
    /**
     * 添加购物车
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        Long userId = UserContext.getCurrentId();
        if (userId == null) {
            throw new ShoppingCartBusinessException("用户未登录");
        }
        if (shoppingCartDTO.getDishId() == null && shoppingCartDTO.getSetmealId() == null) {
            throw new ShoppingCartBusinessException("菜品或套餐不能同时为空");
        }

        // 查询该用户是否已有相同菜品/套餐（含口味）的购物车记录
        List<ShoppingCart> existCarts = shoppingCartMapper.selectList(
                new LambdaQueryWrapper<ShoppingCart>()
                        .eq(ShoppingCart::getUserId, userId)
                        .eq(shoppingCartDTO.getDishId() != null, ShoppingCart::getDishId, shoppingCartDTO.getDishId())
                        .eq(shoppingCartDTO.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCartDTO.getSetmealId())
                        .eq(shoppingCartDTO.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor()));

        // 已存在则数量+1，避免重复记录
        if (!existCarts.isEmpty()) {
            ShoppingCart cart = existCarts.get(0);
            cart.setNumber(cart.getNumber() == null ? 1 : cart.getNumber() + 1);
            shoppingCartMapper.updateById(cart);
            return;
        }

        // 组装购物车记录：补充商品名称、图片、单价、创建时间（数据库 NOT NULL 字段）
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        shoppingCart.setDishId(shoppingCartDTO.getDishId());
        shoppingCart.setSetmealId(shoppingCartDTO.getSetmealId());
        shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());

        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
            if (dish == null) {
                throw new ShoppingCartBusinessException("菜品不存在");
            }
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else {
            Setmeal setmeal = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
            if (setmeal == null) {
                throw new ShoppingCartBusinessException("套餐不存在");
            }
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }

        shoppingCartMapper.insert(shoppingCart);
    }

    /**
     * 删除购物车
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        Long userId = UserContext.getCurrentId();
        if (userId == null) {
            throw new ShoppingCartBusinessException("用户未登录");
        }
        if (shoppingCartDTO.getDishId() == null && shoppingCartDTO.getSetmealId() == null) {
            throw new ShoppingCartBusinessException("菜品或套餐不能同时为空");
        }
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(shoppingCartDTO.getDishId() != null, ShoppingCart::getDishId, shoppingCartDTO.getDishId())
                .eq(shoppingCartDTO.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCartDTO.getSetmealId())
                .eq(shoppingCartDTO.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor())
        );
    }

    @Override
    public void clean() {

        Long userId = UserContext.getCurrentId();
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));

    }

}
