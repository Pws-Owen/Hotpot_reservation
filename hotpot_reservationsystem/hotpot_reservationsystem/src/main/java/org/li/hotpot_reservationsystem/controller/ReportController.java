package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.dto.CustomerReportDTO;
import org.li.hotpot_reservationsystem.dto.InventoryReportDTO;
import org.li.hotpot_reservationsystem.dto.SalesReportDTO;
import org.li.hotpot_reservationsystem.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 报表Controller
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取销售报表
     */
    @GetMapping("/sales")
    public Result<SalesReportDTO> getSalesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            
            SalesReportDTO report = reportService.getSalesReport(start, end);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("获取销售报表失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户分析报表
     */
    @GetMapping("/customer")
    public Result<CustomerReportDTO> getCustomerReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            
            CustomerReportDTO report = reportService.getCustomerReport(start, end);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("获取客户分析报表失败: " + e.getMessage());
        }
    }

    /**
     * 获取库存报表
     */
    @GetMapping("/inventory")
    public Result<InventoryReportDTO> getInventoryReport() {
        try {
            InventoryReportDTO report = reportService.getInventoryReport();
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("获取库存报表失败: " + e.getMessage());
        }
    }
}

