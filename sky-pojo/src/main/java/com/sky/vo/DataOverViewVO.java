package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 数据概览 VO：管理端"数据统计/工作台"的汇总数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataOverViewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 今日营业额（金额用 BigDecimal 保证精度） */
    private BigDecimal turnover;

    /** 今日有效订单数 */
    private Integer validOrderCount;

    /** 今日订单完成率 */
    private Double orderCompletionRate;

    /** 今日平均客单价（金额用 BigDecimal 保证精度） */
    private BigDecimal unitPrice;

    /** 今日新增用户数 */
    private Integer newUsers;

    /** 今日待接单数 */
    private Integer waitingOrders;

    /** 今日待派送数 */
    private Integer deliveredOrders;

    /** 今日已完成数 */
    private Integer completedOrders;

    /** 今日已取消数 */
    private Integer cancelledOrders;

    /** 今日全部订单数 */
    private Integer allOrders;

    /** 菜品起售数 */
    private Integer dishSold;

    /** 菜品停售数 */
    private Integer dishDiscontinued;

    /** 套餐起售数 */
    private Integer setmealSold;

    /** 套餐停售数 */
    private Integer setmealDiscontinued;
}
