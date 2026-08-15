package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 报表统计 Mapper：为"数据统计"页面提供各类聚合查询。
 */
@Mapper
public interface ReportMapper {

    /**
     * 统计指定时间区间内，每天已完成订单的营业额（按天分组求和）
     * 返回元素形如 {date=2026-08-13, turnover=75.00}
     */
    @Select("select DATE_FORMAT(order_time, '%Y-%m-%d') date, sum(amount) turnover " +
            "from orders " +
            "where status = 5 and order_time between #{begin} and #{end} " +
            "group by date order by date")
    List<Map<String, Object>> getTurnoverStatistics(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 统计指定时间区间内，每天新增的用户数（按天分组计数）
     * 返回元素形如 {date=2026-08-13, count=3}
     */
    @Select("select DATE_FORMAT(create_time, '%Y-%m-%d') date, count(id) count " +
            "from `user` " +
            "where create_time between #{begin} and #{end} " +
            "group by date order by date")
    List<Map<String, Object>> getUserStatistics(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 统计截至指定时间点（不含该时刻）的用户总量
     */
    @Select("select count(id) from `user` where create_time < #{end}")
    Integer getUserTotal(@Param("end") LocalDateTime end);

    /**
     * 统计指定时间区间内，每天的订单总数（按天分组计数）
     * 返回元素形如 {date=2026-08-13, count=5}
     */
    @Select("select DATE_FORMAT(order_time, '%Y-%m-%d') date, count(id) count " +
            "from orders " +
            "where order_time between #{begin} and #{end} " +
            "group by date order by date")
    List<Map<String, Object>> getOrderCount(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 统计指定时间区间内，每天指定状态的订单数（按天分组计数）
     * 返回元素形如 {date=2026-08-13, count=2}
     */
    @Select("select DATE_FORMAT(order_time, '%Y-%m-%d') date, count(id) count " +
            "from orders " +
            "where status = #{status} and order_time between #{begin} and #{end} " +
            "group by date order by date")
    List<Map<String, Object>> getOrderCountByStatus(@Param("begin") LocalDateTime begin,
                                                    @Param("end") LocalDateTime end,
                                                    @Param("status") Integer status);

    /**
     * 统计指定时间区间内，每天状态在给定列表中的订单数（按天分组计数，如有效订单 3,4,5）
     * 返回元素形如 {date=2026-08-13, count=2}
     */
    @Select("<script>" +
            "select DATE_FORMAT(order_time, '%Y-%m-%d') date, count(id) count " +
            "from orders " +
            "where order_time between #{begin} and #{end} " +
            "and status in " +
            "<foreach collection='statusList' item='s' open='(' separator=',' close=')'>#{s}</foreach> " +
            "group by date order by date" +
            "</script>")
    List<Map<String, Object>> getOrderCountByStatusList(@Param("begin") LocalDateTime begin,
                                                        @Param("end") LocalDateTime end,
                                                        @Param("statusList") List<Integer> statusList);

    /**
     * 统计指定时间区间内，销量排名前 10 的菜品/套餐（按名称分组求和）
     * 返回元素形如 {name=辣子鸡, number=12}
     */
    @Select("select od.name name, sum(od.number) number " +
            "from order_detail od " +
            "left join orders o on od.order_id = o.id " +
            "where o.status = 5 and o.order_time between #{begin} and #{end} " +
            "group by od.name " +
            "order by number desc " +
            "limit 10")
    List<Map<String, Object>> getSalesTop10(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
