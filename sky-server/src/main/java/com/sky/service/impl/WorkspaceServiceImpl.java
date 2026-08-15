package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * 工作台（管理端首页）统计服务实现。
 *
 * 订单状态说明（见 Orders 实体常量）：
 * 1 待付款 PENDING_PAYMENT，2 待接单 TO_BE_CONFIRMED，3 已接单 CONFIRMED，
 * 4 派送中 DELIVERY_IN_PROGRESS，5 已完成 COMPLETED，6 已取消 CANCELLED
 */
@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 查询指定日期的营业数据：
     * 营业额              = 今日状态为已完成(5)的订单金额合计
     * 有效订单数          = 今日状态为 3/4/5（已接单、派送中、已完成）的订单数
     * 订单完成率          = 有效订单数 / 今日全部订单数
     * 平均客单价          = 营业额 / 有效订单数（每单平均金额）
     * 新增用户数          = 今日注册的用户数
     */
    @Override
    public BusinessDataVO getBusinessData(LocalDate date) {
        LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

        // 营业额：今日已完成订单的金额合计
        BigDecimal turnover = orderMapper.sumAmountByStatusAndTime(beginTime, endTime, Orders.COMPLETED);
        if (turnover == null) {
            turnover = BigDecimal.ZERO;
        }

        // 有效订单：状态在 (3 已接单, 4 派送中, 5 已完成) 的订单
        List<Integer> validStatusList = Arrays.asList(
                Orders.CONFIRMED, Orders.DELIVERY_IN_PROGRESS, Orders.COMPLETED);
        Integer validOrderCount = orderMapper.countByTimeAndStatusIn(beginTime, endTime, validStatusList);
        if (validOrderCount == null) {
            validOrderCount = 0;
        }

        // 今日全部订单数
        Integer orderCount = orderMapper.countByTimeAndStatus(beginTime, endTime, null);
        if (orderCount == null) {
            orderCount = 0;
        }

        // 订单完成率，注意避免除数为 0
        Double orderCompletionRate = 0.0;
        if (orderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / orderCount;
        }

        // 平均客单价，注意避免除数为 0；用 BigDecimal 精确计算，保留 2 位小数
        BigDecimal unitPrice = BigDecimal.ZERO;
        if (validOrderCount != 0) {
            // 除以订单数，保留2位小数，采用四舍五入模式
            unitPrice = turnover.divide(
                    new BigDecimal(validOrderCount),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        // 今日新增用户数
        Integer newUsers = userMapper.countByTime(beginTime, endTime);
        if (newUsers == null) {
            newUsers = 0;
        }

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    /**
     * 今日订单概览：
     * waitingOrders    = 待接单(2)，等待商家接单
     * deliveredOrders  = 已接单(3)，已接单待派送
     * completedOrders  = 已完成(5)
     * cancelledOrders  = 已取消(6)
     * allOrders        = 今日全部订单
     */
    @Override
    public OrderOverViewVO getOrderOverView() {
        LocalDateTime beginTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Integer allOrders = orderMapper.countByTimeAndStatus(beginTime, endTime, null);
        Integer waitingOrders = orderMapper.countByTimeAndStatus(beginTime, endTime, Orders.TO_BE_CONFIRMED);
        Integer deliveredOrders = orderMapper.countByTimeAndStatus(beginTime, endTime, Orders.CONFIRMED);
        Integer completedOrders = orderMapper.countByTimeAndStatus(beginTime, endTime, Orders.COMPLETED);
        Integer cancelledOrders = orderMapper.countByTimeAndStatus(beginTime, endTime, Orders.CANCELLED);

        return OrderOverViewVO.builder()
                .allOrders(nullToZero(allOrders))
                .waitingOrders(nullToZero(waitingOrders))
                .deliveredOrders(nullToZero(deliveredOrders))
                .completedOrders(nullToZero(completedOrders))
                .cancelledOrders(nullToZero(cancelledOrders))
                .build();
    }

    /**
     * 菜品概览：状态 1 起售中，0 已停售
     */
    @Override
    public DishOverViewVO getDishOverView() {
        Integer sold = dishMapper.countByStatus(StatusConstant.ENABLE);
        Integer discontinued = dishMapper.countByStatus(StatusConstant.DISABLE);
        return DishOverViewVO.builder()
                .sold(nullToZero(sold))
                .discontinued(nullToZero(discontinued))
                .build();
    }

    /**
     * 套餐概览：状态 1 起售中，0 已停售
     */
    @Override
    public SetmealOverViewVO getSetmealOverView() {
        Integer sold = setmealMapper.countByStatus(StatusConstant.ENABLE);
        Integer discontinued = setmealMapper.countByStatus(StatusConstant.DISABLE);
        return SetmealOverViewVO.builder()
                .sold(nullToZero(sold))
                .discontinued(nullToZero(discontinued))
                .build();
    }

    private Integer nullToZero(Integer value) {
        return value == null ? 0 : value;
    }
}
