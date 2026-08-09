package com.sky.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐菜品关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealDish implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    //套餐id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long setmealId;

    //菜品id
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dishId;

    //菜品名称 （冗余字段）
    private String name;

    //菜品原价
    private BigDecimal price;

    //份数
    private Integer copies;

}
