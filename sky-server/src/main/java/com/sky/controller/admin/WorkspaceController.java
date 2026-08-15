package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 工作台控制器：管理端首页（工作台）展示的数据。
 * 所有接口都在 /admin/workspace 下，需要携带管理员 JWT token 才能访问。
 */
@RestController("adminWorkspaceController")
@RequestMapping("/admin/workspace")
@Slf4j
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 今日营业数据（营业额、有效订单数、订单完成率、平均客单价、新增用户数）
     */
    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData() {
        return Result.success(workspaceService.getBusinessData(LocalDate.now()));
    }

    /**
     * 今日订单概览（待接单 / 待派送 / 已完成 / 已取消 / 全部订单）
     */
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders() {
        return Result.success(workspaceService.getOrderOverView());
    }

    /**
     * 菜品概览（起售中 / 已停售）
     */
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes() {
        return Result.success(workspaceService.getDishOverView());
    }

    /**
     * 套餐概览（起售中 / 已停售）
     */
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals() {
        return Result.success(workspaceService.getSetmealOverView());
    }
}
