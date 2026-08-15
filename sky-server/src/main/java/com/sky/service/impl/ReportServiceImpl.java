package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ReportMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DataOverViewVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.SetmealOverViewVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表统计服务实现：负责"数据统计"页面的各类统计与 Excel 导出。
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 生成 [begin, end] 之间的完整日期列表（含首尾两天）
     */
    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            dateList.add(current);
            current = current.plusDays(1);
        }
        return dateList;
    }

    /**
     * 把某天的 LocalDate 转成当天起始时刻（00:00:00）
     */
    private LocalDateTime atStart(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * 把某天的 LocalDate 转成当天结束时刻（23:59:59.999999999）
     */
    private LocalDateTime atEnd(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    /**
     * 把按天分组的统计结果转成 Map：key 为日期字符串，value 为统计值
     */
    private Map<String, Object> toDateMap(List<Map<String, Object>> rows, String valueKey) {
        Map<String, Object> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            map.put(String.valueOf(row.get("date")), row.get(valueKey));
        }
        return map;
    }

    /**
     * 每日营业额统计：营业额只统计"已完成"(5)的订单
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        Map<String, Object> turnoverMap =
                toDateMap(reportMapper.getTurnoverStatistics(atStart(begin), atEnd(end)), "turnover");

        StringBuilder dateStr = new StringBuilder();
        StringBuilder turnoverStr = new StringBuilder();
        for (LocalDate date : dateList) {
            dateStr.append(date).append(",");
            Object turnover = turnoverMap.get(date.toString());
            // 当天没有已完成订单时营业额计为 0
            turnoverStr.append(turnover == null ? "0.0" : turnover).append(",");
        }
        // 去掉末尾多余的逗号
        dateStr.deleteCharAt(dateStr.length() - 1);
        turnoverStr.deleteCharAt(turnoverStr.length() - 1);

        return TurnoverReportVO.builder()
                .dateList(dateStr.toString())
                .turnoverList(turnoverStr.toString())
                .build();
    }

    /**
     * 用户统计：每天的新增用户数 + 截至每天的累计用户总量
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        Map<String, Object> newUserMap =
                toDateMap(reportMapper.getUserStatistics(atStart(begin), atEnd(end)), "count");

        StringBuilder dateStr = new StringBuilder();
        StringBuilder newUserStr = new StringBuilder();
        StringBuilder totalUserStr = new StringBuilder();
        // 起始日之前的历史用户总数作为累计基数
        Integer totalUser = reportMapper.getUserTotal(atStart(begin));
        if (totalUser == null) {
            totalUser = 0;
        }
        for (LocalDate date : dateList) {
            dateStr.append(date).append(",");
            Object newUser = newUserMap.get(date.toString());
            int newUserCount = newUser == null ? 0 : Integer.parseInt(newUser.toString());
            totalUser += newUserCount;
            newUserStr.append(newUserCount).append(",");
            totalUserStr.append(totalUser).append(",");
        }
        dateStr.deleteCharAt(dateStr.length() - 1);
        newUserStr.deleteCharAt(newUserStr.length() - 1);
        totalUserStr.deleteCharAt(totalUserStr.length() - 1);

        return UserReportVO.builder()
                .dateList(dateStr.toString())
                .newUserList(newUserStr.toString())
                .totalUserList(totalUserStr.toString())
                .build();
    }

    /**
     * 订单统计：每天的订单总数 + 有效订单数（已接单 3 / 派送中 4 / 已完成 5）
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);
        Map<String, Object> orderCountMap =
                toDateMap(reportMapper.getOrderCount(atStart(begin), atEnd(end)), "count");
        Map<String, Object> validOrderCountMap =
                toDateMap(reportMapper.getOrderCountByStatusList(atStart(begin), atEnd(end),
                        java.util.Arrays.asList(Orders.CONFIRMED, Orders.DELIVERY_IN_PROGRESS, Orders.COMPLETED)), "count");

        StringBuilder dateStr = new StringBuilder();
        StringBuilder orderCountStr = new StringBuilder();
        StringBuilder validOrderCountStr = new StringBuilder();
        int totalOrderCount = 0;
        int validOrderCount = 0;
        for (LocalDate date : dateList) {
            dateStr.append(date).append(",");
            Object orderCount = orderCountMap.get(date.toString());
            int dayOrderCount = orderCount == null ? 0 : Integer.parseInt(orderCount.toString());
            Object validOrderCountObj = validOrderCountMap.get(date.toString());
            int dayValidOrderCount = validOrderCountObj == null ? 0 : Integer.parseInt(validOrderCountObj.toString());
            totalOrderCount += dayOrderCount;
            validOrderCount += dayValidOrderCount;
            orderCountStr.append(dayOrderCount).append(",");
            validOrderCountStr.append(dayValidOrderCount).append(",");
        }
        dateStr.deleteCharAt(dateStr.length() - 1);
        orderCountStr.deleteCharAt(orderCountStr.length() - 1);
        validOrderCountStr.deleteCharAt(validOrderCountStr.length() - 1);

        // 订单完成率 = 有效订单数 / 订单总数（避免除数为 0）
        Double orderCompletionRate = totalOrderCount == 0 ? 0.0
                : (double) validOrderCount / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(dateStr.toString())
                .orderCountList(orderCountStr.toString())
                .validOrderCountList(validOrderCountStr.toString())
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名前 10：按菜品/套餐名称汇总销量
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        List<Map<String, Object>> top10 = reportMapper.getSalesTop10(atStart(begin), atEnd(end));

        StringBuilder nameStr = new StringBuilder();
        StringBuilder numberStr = new StringBuilder();
        for (Map<String, Object> row : top10) {
            nameStr.append(row.get("name")).append(",");
            numberStr.append(row.get("number")).append(",");
        }
        if (nameStr.length() > 0) {
            nameStr.deleteCharAt(nameStr.length() - 1);
            numberStr.deleteCharAt(numberStr.length() - 1);
        }

        return SalesTop10ReportVO.builder()
                .nameList(nameStr.toString())
                .numberList(numberStr.toString())
                .build();
    }

    /**
     * 数据概览：复用工作台统计逻辑，汇总今日营业数据、订单概况、菜品/套餐概况
     */
    @Override
    public DataOverViewVO getDataOverView() {
        // 今日营业数据
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDate.now());
        // 今日订单概况
        OrderOverViewVO orderOverView = workspaceService.getOrderOverView();
        // 菜品/套餐概况
        DishOverViewVO dishOverView = workspaceService.getDishOverView();
        SetmealOverViewVO setmealOverView = workspaceService.getSetmealOverView();

        return DataOverViewVO.builder()
                .turnover(businessData.getTurnover())
                .validOrderCount(businessData.getValidOrderCount())
                .orderCompletionRate(businessData.getOrderCompletionRate())
                .unitPrice(businessData.getUnitPrice())
                .newUsers(businessData.getNewUsers())
                .waitingOrders(orderOverView.getWaitingOrders())
                .deliveredOrders(orderOverView.getDeliveredOrders())
                .completedOrders(orderOverView.getCompletedOrders())
                .cancelledOrders(orderOverView.getCancelledOrders())
                .allOrders(orderOverView.getAllOrders())
                .dishSold(dishOverView.getSold())
                .dishDiscontinued(dishOverView.getDiscontinued())
                .setmealSold(setmealOverView.getSold())
                .setmealDiscontinued(setmealOverView.getDiscontinued())
                .build();
    }

    /**
     * 导出营业数据报表：动态生成 Excel 并通过响应流下载
     */
    @Override
    public void exportBusinessData(LocalDate begin, LocalDate end, HttpServletResponse response) {
        // 1. 汇总数据（区间内总计）
        Map<String, Object> turnoverMap =
                toDateMap(reportMapper.getTurnoverStatistics(atStart(begin), atEnd(end)), "turnover");
        BigDecimal turnover = BigDecimal.ZERO;
        for (Object value : turnoverMap.values()) {
            if (value != null) {
                turnover = turnover.add(new BigDecimal(value.toString()));
            }
        }
        Integer validOrderCount = orderMapper.countByTimeAndStatusIn(
                begin.atStartOfDay(), end.atTime(23, 59, 59),
                java.util.Arrays.asList(Orders.CONFIRMED, Orders.DELIVERY_IN_PROGRESS, Orders.COMPLETED));
        Integer totalOrderCount = orderMapper.countByTimeAndStatus(
                begin.atStartOfDay(), end.atTime(23, 59, 59), null);
        Double orderCompletionRate = (totalOrderCount == null || totalOrderCount == 0) ? 0.0
                : (double) (validOrderCount == null ? 0 : validOrderCount) / totalOrderCount;
        Integer newUsers = userMapper.countByTime(begin.atStartOfDay(), end.atTime(23, 59, 59));

        // 2. 动态创建工作簿并填充数据
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ServletOutputStream out = response.getOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("运营数据报表");

            // 表头
            XSSFRow header = sheet.createRow(0);
            String[] headers = {"日期", "营业额(元)", "有效订单数", "订单总数", "订单完成率"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            // 汇总行
            XSSFRow summary = sheet.createRow(1);
            summary.createCell(0).setCellValue(begin + " 至 " + end);
            summary.createCell(1).setCellValue(turnover.doubleValue());
            summary.createCell(2).setCellValue(validOrderCount == null ? 0 : validOrderCount);
            summary.createCell(3).setCellValue(totalOrderCount == null ? 0 : totalOrderCount);
            summary.createCell(4).setCellValue(orderCompletionRate);

            // 每日明细行
            List<LocalDate> dateList = getDateList(begin, end);
            Map<String, Object> dailyOrderCountMap =
                    toDateMap(reportMapper.getOrderCount(atStart(begin), atEnd(end)), "count");
            Map<String, Object> dailyValidOrderCountMap =
                    toDateMap(reportMapper.getOrderCountByStatusList(atStart(begin), atEnd(end),
                            java.util.Arrays.asList(Orders.CONFIRMED, Orders.DELIVERY_IN_PROGRESS, Orders.COMPLETED)), "count");

            int rowIndex = 2;
            for (LocalDate date : dateList) {
                XSSFRow row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(date.toString());
                Object dayTurnover = turnoverMap.get(date.toString());
                row.createCell(1).setCellValue(dayTurnover == null ? 0.0
                        : Double.parseDouble(dayTurnover.toString()));
                Object dayValid = dailyValidOrderCountMap.get(date.toString());
                Object dayTotal = dailyOrderCountMap.get(date.toString());
                int dv = dayValid == null ? 0 : Integer.parseInt(dayValid.toString());
                int dt = dayTotal == null ? 0 : Integer.parseInt(dayTotal.toString());
                row.createCell(2).setCellValue(dv);
                row.createCell(3).setCellValue(dt);
                row.createCell(4).setCellValue(dt == 0 ? 0.0 : (double) dv / dt);
            }

            // 列宽自适应
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 18 * 256);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=business_data_" + begin + "_" + end + ".xlsx");
            workbook.write(out);
        } catch (IOException e) {
            log.error("导出营业数据报表失败", e);
        }
    }
}
