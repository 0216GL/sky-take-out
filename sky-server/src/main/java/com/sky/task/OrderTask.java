package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrders() {
        log.info("处理超时订单...");

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getPayStatus, Orders.UN_PAID);
        queryWrapper.eq(Orders::getStatus, Orders.PENDING_PAYMENT);
        // 下单时间早于15分钟前仍未支付 → 超时
        queryWrapper.lt(Orders::getOrderTime, java.time.LocalDateTime.now().minusMinutes(15));

        List<Orders> ordersList = orderMapper.selectList(queryWrapper);

        for (Orders orders : ordersList) {
            log.info("处理超时订单：{}", orders.getId());
            orders.setCancelReason("订单超时未支付，系统自动取消");
            orders.setCancelTime(LocalDateTime.now());
            orders.setStatus(Orders.CANCELLED);
            orderMapper.updateById(orders);
        }
    }

    /**
     * 处理一直处在派送中的订单
     */
    @Scheduled(cron = "10 * * * * ?")
    public void processDeliveryOrders() {
        log.info("处理一直处在派送中的订单...");
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS);
        queryWrapper.gt(Orders::getOrderTime, java.time.LocalDateTime.now().minusSeconds(1));

        List<Orders> ordersList = orderMapper.selectList(queryWrapper);

        for (Orders orders : ordersList) {
            orders.setCancelTime(LocalDateTime.now());
            orders.setStatus(Orders.COMPLETED);
            orderMapper.updateById(orders);
        }
    }
}
