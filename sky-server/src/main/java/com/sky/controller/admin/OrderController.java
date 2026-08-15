package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersCancelDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderVO;
import com.sky.vo.OrderStatisticsVO;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j

public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单列表
     */
    @GetMapping("/conditionSearch")
    public Result conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        return Result.success(orderService.pageQuery(ordersPageQueryDTO));
    }

    /**
     * 订单详情
     */
    @GetMapping("/details/{id}")
    public Result details(@PathVariable Long id){
        OrderVO orderVO = orderService.getByIdWithDetail(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     */
    @PutMapping("/rejection")
    public Result reject(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        orderService.reject(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 订单状态统计
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 派送订单
     */
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     */
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        orderService.complete(id);
        return Result.success();
    }
}
