package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 数据概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDataVO implements Serializable {

    /** 营业额（金额用 BigDecimal 保证精度） */
    private BigDecimal turnover;

    /** 有效订单数 */
    private Integer validOrderCount;

    /** 订单完成率 */
    private Double orderCompletionRate;

    /** 平均客单价（金额用 BigDecimal 保证精度） */
    private BigDecimal unitPrice;

    /** 新增用户数 */
    private Integer newUsers;

}
