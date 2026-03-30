package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.li.hotpot_reservationsystem.dto.DashboardStatsDTO;
import org.li.hotpot_reservationsystem.entity.CustomerOrder;
import org.li.hotpot_reservationsystem.entity.Reservation;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.service.CustomerOrderService;
import org.li.hotpot_reservationsystem.service.DashboardService;
import org.li.hotpot_reservationsystem.service.ReservationService;
import org.li.hotpot_reservationsystem.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘Service实现类
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CustomerOrderService orderService;

    @Autowired
    private SysUserService userService;

    @Override
    public DashboardStatsDTO getDashboardStats(Integer days) {
        // 默认7天，如果传入的值不是7或30，则使用7
        if (days == null || (days != 7 && days != 30)) {
            days = 7;
        }
        
        DashboardStatsDTO stats = new DashboardStatsDTO();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        // 1. 今日预约数
        LambdaQueryWrapper<Reservation> reservationWrapper = new QueryWrapper<Reservation>().lambda();
        reservationWrapper.ge(Reservation::getReservationDate, today);
        reservationWrapper.lt(Reservation::getReservationDate, today.plusDays(1));
        Long todayReservations = reservationService.count(reservationWrapper);
        stats.setTodayReservations(todayReservations != null ? todayReservations : 0L);

        // 2. 今日订单数
        LambdaQueryWrapper<CustomerOrder> orderWrapper = new QueryWrapper<CustomerOrder>().lambda();
        orderWrapper.ge(CustomerOrder::getCreateTime, todayStart);
        orderWrapper.lt(CustomerOrder::getCreateTime, todayEnd);
        Long todayOrders = orderService.count(orderWrapper);
        stats.setTodayOrders(todayOrders != null ? todayOrders : 0L);

        // 3. 今日营收（已支付订单的实付金额总和）
        orderWrapper.clear();
        orderWrapper.ge(CustomerOrder::getCreateTime, todayStart);
        orderWrapper.lt(CustomerOrder::getCreateTime, todayEnd);
        orderWrapper.eq(CustomerOrder::getStatus, 1); // 已支付状态
        List<CustomerOrder> paidOrders = orderService.list(orderWrapper);
        BigDecimal todayRevenue = paidOrders.stream()
                .map(order -> order.getActualAmount() != null ? order.getActualAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTodayRevenue(todayRevenue);

        // 4. 总用户数（只统计客户，userType=2，排除管理员和前台）
        LambdaQueryWrapper<SysUser> userWrapper = new QueryWrapper<SysUser>().lambda();
        userWrapper.eq(SysUser::getUserType, 2); // 只统计客户类型
        Long totalUsers = userService.count(userWrapper);
        stats.setTotalUsers(totalUsers != null ? totalUsers : 0L);

        // 5. 预约趋势（根据days参数决定是7天还是30天）
        List<Map<String, Object>> reservationTrend = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MM/dd");
        
        // 根据days计算起始日期
        int startDays = days - 1; // 从今天往前推days-1天
        
        for (int i = startDays; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDate nextDate = date.plusDays(1);
            
            LambdaQueryWrapper<Reservation> trendWrapper = new QueryWrapper<Reservation>().lambda();
            trendWrapper.ge(Reservation::getReservationDate, date);
            trendWrapper.lt(Reservation::getReservationDate, nextDate);
            Long count = reservationService.count(trendWrapper);
            
            Map<String, Object> trendItem = new HashMap<>();
            trendItem.put("date", date.format(dayFormatter));
            trendItem.put("count", count != null ? count : 0);
            reservationTrend.add(trendItem);
        }
        stats.setReservationTrend(reservationTrend);

        // 6. 订单状态分布
        List<Map<String, Object>> orderStatusDistribution = new ArrayList<>();
        
        // 查询所有订单的状态分布
        List<CustomerOrder> allOrders = orderService.list();
        Map<Integer, Long> statusCountMap = new HashMap<>();
        for (CustomerOrder order : allOrders) {
            Integer status = order.getStatus();
            statusCountMap.put(status, statusCountMap.getOrDefault(status, 0L) + 1);
        }
        
        // 状态映射：0-待支付，1-已支付，2-制作中，3-已完成，4-已取消，5-已退款
        Map<Integer, String> statusNameMap = new HashMap<>();
        statusNameMap.put(0, "待支付");
        statusNameMap.put(1, "已支付");
        statusNameMap.put(2, "制作中");
        statusNameMap.put(3, "已完成");
        statusNameMap.put(4, "已取消");
        statusNameMap.put(5, "已退款");
        
        for (Map.Entry<Integer, Long> entry : statusCountMap.entrySet()) {
            Map<String, Object> statusItem = new HashMap<>();
            statusItem.put("name", statusNameMap.getOrDefault(entry.getKey(), "未知"));
            statusItem.put("value", entry.getValue());
            orderStatusDistribution.add(statusItem);
        }
        
        stats.setOrderStatusDistribution(orderStatusDistribution);

        return stats;
    }
}

