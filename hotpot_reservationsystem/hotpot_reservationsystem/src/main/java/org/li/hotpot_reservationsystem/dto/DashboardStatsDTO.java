package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计数据DTO
 */
@Data
public class DashboardStatsDTO {
    /**
     * 今日预约数
     */
    private Long todayReservations;

    /**
     * 今日订单数
     */
    private Long todayOrders;

    /**
     * 今日营收
     */
    private BigDecimal todayRevenue;

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 近7天预约趋势数据
     * key: 日期（yyyy-MM-dd），value: 预约数量
     */
    private List<Map<String, Object>> reservationTrend;

    /**
     * 订单状态分布
     * key: 状态名称，value: 数量
     */
    private List<Map<String, Object>> orderStatusDistribution;
}

