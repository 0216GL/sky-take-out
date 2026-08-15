package com.sky.service;


import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersCancelDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderVO;
import com.sky.vo.OrderStatisticsVO;

import java.util.List;

public interface OrderService {

    /**
     * 用户下单
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 用户支付（模拟支付）
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO);

    /**
     * 用户查看历史订单
     */
    PageResult historyOrders(int page, int pageSize, String status);

    /**
     * 用户查询订单详情（含订单明细）
     */
    OrderVO userOrderDetail(Long id);

    /**
     * 用户再来一单：把历史订单的菜品重新加入购物车
     */
    void repetition(Long id);

    /**
     * 用户催单：向管理端推送催单消息
     */
    void reminder(Long id);

    /**
     * 订单分页查询
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单详情（含订单明细）
     */
    OrderVO getByIdWithDetail(Long id);

    /**
     * 接单
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     */
    void reject(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 订单状态统计
     */
    OrderStatisticsVO statistics();

    /**
     * 派送订单
     */
    void delivery(Long id);

    /**
     * 完成订单
     */
    void complete(Long id);
}
