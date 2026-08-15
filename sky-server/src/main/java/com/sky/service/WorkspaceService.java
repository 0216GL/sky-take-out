package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDate;

public interface WorkspaceService {

    /**
     * 查询指定日期的营业数据（工作台首页顶部区域）
     */
    BusinessDataVO getBusinessData(LocalDate date);

    /**
     * 查询今日订单概览统计
     */
    OrderOverViewVO getOrderOverView();

    /**
     * 查询菜品概览（起售中 / 已停售）
     */
    DishOverViewVO getDishOverView();

    /**
     * 查询套餐概览（起售中 / 已停售）
     */
    SetmealOverViewVO getSetmealOverView();
}
