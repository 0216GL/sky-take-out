package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.DataOverViewVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 报表统计控制器：管理端"数据统计"页面的所有接口。
 */
@RestController("adminReportController")
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计（按日）
     */
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }

    /**
     * 用户统计（按日）
     */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    /**
     * 订单统计（按日）
     */
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.getOrdersStatistics(begin, end));
    }

    /**
     * 销量排名前 10
     */
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return Result.success(reportService.getSalesTop10(begin, end));
    }

    /**
     * 数据概览（今日营业数据 + 订单概况 + 菜品/套餐概况）
     */
    @GetMapping("/dataOverView")
    public Result<DataOverViewVO> dataOverView() {
        return Result.success(reportService.getDataOverView());
    }

    /**
     * 导出营业数据报表（Excel 下载）
     * begin/end 可选，不传时默认导出最近 30 天
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        // 前端"数据导出"按钮不传日期，默认导出最近 30 天
        if (begin == null || end == null) {
            end = LocalDate.now();
            begin = end.minusDays(29);
        }
        reportService.exportBusinessData(begin, end, response);
    }
}
