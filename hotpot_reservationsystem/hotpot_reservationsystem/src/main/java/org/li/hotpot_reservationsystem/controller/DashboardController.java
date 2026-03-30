package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.dto.DashboardStatsDTO;
import org.li.hotpot_reservationsystem.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘Controller
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     * @param days 预约趋势天数，可选值：7或30，默认7
     */
    @GetMapping("/stats")
    public Result<DashboardStatsDTO> getDashboardStats(Integer days) {
        try {
            // 默认7天，如果传入的值不是7或30，则使用7
            if (days == null || (days != 7 && days != 30)) {
                days = 7;
            }
            DashboardStatsDTO stats = dashboardService.getDashboardStats(days);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
}

