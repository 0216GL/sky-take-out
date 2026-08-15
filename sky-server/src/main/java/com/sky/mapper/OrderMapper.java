package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    /**
     * 统计某状态订单在指定时间段内的金额合计（用于工作台营业额）
     */
    @Select("select sum(amount) from orders " +
            "where status = #{status} and order_time between #{begin} and #{end}")
    BigDecimal sumAmountByStatusAndTime(@Param("begin") LocalDateTime begin,
                                        @Param("end") LocalDateTime end,
                                        @Param("status") Integer status);

    /**
     * 统计指定时间段内、指定状态的订单数量（status 传 null 表示统计全部）
     */
    @Select("<script>" +
            "select count(id) from orders " +
            "where order_time between #{begin} and #{end} " +
            "<if test='status != null'> and status = #{status} </if>" +
            "</script>")
    Integer countByTimeAndStatus(@Param("begin") LocalDateTime begin,
                                 @Param("end") LocalDateTime end,
                                 @Param("status") Integer status);

    /**
     * 统计指定时间段内、状态在给定列表中的订单数量（例如有效订单 3,4,5）
     */
    @Select("<script>" +
            "select count(id) from orders " +
            "where order_time between #{begin} and #{end} " +
            "and status in " +
            "<foreach collection='statusList' item='s' open='(' separator=',' close=')'>#{s}</foreach>" +
            "</script>")
    Integer countByTimeAndStatusIn(@Param("begin") LocalDateTime begin,
                                   @Param("end") LocalDateTime end,
                                   @Param("statusList") List<Integer> statusList);
}
