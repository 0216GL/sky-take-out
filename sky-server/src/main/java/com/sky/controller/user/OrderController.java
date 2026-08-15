package com.sky.controller.user;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.entity.Orders;
import com.sky.result.Result;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userOrderController")
@Slf4j
@RequestMapping("/user/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){

        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 用户支付（模拟支付：直接置为支付成功，不调用微信接口）
     */
    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO){
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 用户查看历史订单
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(@RequestParam int page, @RequestParam int pageSize, @RequestParam String status){
        PageResult pageResult = orderService.historyOrders(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 用户查询订单详情
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id){
        return Result.success(orderService.userOrderDetail(id));
    }

    /**
     * 用户再来一单
     */
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id){
        orderService.repetition(id);
        return Result.success();
    }

    /**
     * 用户催单
     */
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id){
        orderService.reminder(id);
        return Result.success();
    }
}
