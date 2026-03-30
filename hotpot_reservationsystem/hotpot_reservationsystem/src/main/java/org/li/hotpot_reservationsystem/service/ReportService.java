package org.li.hotpot_reservationsystem.service;

import org.li.hotpot_reservationsystem.dto.CustomerReportDTO;
import org.li.hotpot_reservationsystem.dto.InventoryReportDTO;
import org.li.hotpot_reservationsystem.dto.SalesReportDTO;

import java.time.LocalDate;

/**
 * 报表Service接口
 */
public interface ReportService {
    /**
     * 获取销售报表数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售报表数据
     */
    SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate);

    /**
     * 获取客户分析报表数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 客户分析报表数据
     */
    CustomerReportDTO getCustomerReport(LocalDate startDate, LocalDate endDate);

    /**
     * 获取库存报表数据
     * @return 库存报表数据
     */
    InventoryReportDTO getInventoryReport();
}

