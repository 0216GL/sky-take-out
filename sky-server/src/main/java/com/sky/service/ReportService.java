package com.sky.service;

import com.sky.vo.DataOverViewVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 报表统计服务：为"数据统计"页面提供营业额、用户、订单、销量排名等统计。
 */
public interface ReportService {

    /**
     * 统计指定日期区间内的每日营业额
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 统计指定日期区间内的每日新增用户和用户总量
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 统计指定日期区间内的每日订单数和有效订单数
     */
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    /**
     * 统计指定日期区间内的销量排名前 10
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    /**
     * 查询数据概览（今日营业数据 + 订单概况 + 菜品/套餐概况）
     */
    DataOverViewVO getDataOverView();

    /**
     * 导出指定日期区间内的营业数据报表（Excel 下载）
     */
    void exportBusinessData(LocalDate begin, LocalDate end, HttpServletResponse response);
}
