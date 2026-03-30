package org.li.hotpot_reservationsystem.service;

import org.li.hotpot_reservationsystem.dto.DashboardStatsDTO;

/**
 * 仪表盘Service接口
 */
public interface DashboardService {
    /**
     * 获取仪表盘统计数据
     * @param days 预约趋势天数，可选值：7或30
     */
    DashboardStatsDTO getDashboardStats(Integer days);
}

