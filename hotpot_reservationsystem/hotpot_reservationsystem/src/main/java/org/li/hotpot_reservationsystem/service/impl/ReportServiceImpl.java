package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.li.hotpot_reservationsystem.dto.CustomerReportDTO;
import org.li.hotpot_reservationsystem.dto.InventoryReportDTO;
import org.li.hotpot_reservationsystem.dto.SalesReportDTO;
import org.li.hotpot_reservationsystem.entity.CustomerOrder;
import org.li.hotpot_reservationsystem.entity.MenuItem;
import org.li.hotpot_reservationsystem.entity.OrderDetail;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.mapper.CustomerOrderMapper;
import org.li.hotpot_reservationsystem.mapper.MenuItemMapper;
import org.li.hotpot_reservationsystem.mapper.OrderDetailMapper;
import org.li.hotpot_reservationsystem.service.CustomerOrderService;
import org.li.hotpot_reservationsystem.service.MenuItemService;
import org.li.hotpot_reservationsystem.service.ReportService;
import org.li.hotpot_reservationsystem.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报表Service实现类
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private CustomerOrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private CustomerOrderService orderService;

    @Autowired
    private SysUserService userService;

    @Autowired
    private MenuItemService menuItemService;

    @Autowired
    private MenuItemMapper menuItemMapper;

    @Override
    public SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate) {
        SalesReportDTO report = new SalesReportDTO();

        // 如果未指定日期范围，默认查询最近30天
        if (startDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // 查询指定日期范围内的已支付订单（status=1）
        LambdaQueryWrapper<CustomerOrder> orderWrapper = new QueryWrapper<CustomerOrder>().lambda();
        orderWrapper.ge(CustomerOrder::getCreateTime, startDateTime);
        orderWrapper.lt(CustomerOrder::getCreateTime, endDateTime);
        orderWrapper.eq(CustomerOrder::getStatus, 1); // 只统计已支付的订单
        orderWrapper.orderByAsc(CustomerOrder::getCreateTime);

        List<CustomerOrder> orders = orderService.list(orderWrapper);

        // 1. 每日销售统计
        List<SalesReportDTO.DailySalesStat> dailySalesStats = calculateDailySalesStats(orders, startDate, endDate);
        report.setDailySalesStats(dailySalesStats);

        // 2. 菜品销售排行
        List<SalesReportDTO.DishSalesRank> dishSalesRanks = calculateDishSalesRanks(orders);
        report.setDishSalesRanks(dishSalesRanks);

        // 3. 时段分析
        List<SalesReportDTO.TimeAnalysis> timeAnalyses = calculateTimeAnalysis(orders);
        report.setTimeAnalyses(timeAnalyses);

        // 4. 销售明细
        List<SalesReportDTO.SalesDetail> salesDetails = calculateSalesDetails(orders, startDate, endDate);
        report.setSalesDetails(salesDetails);

        return report;
    }

    /**
     * 计算每日销售统计
     */
    private List<SalesReportDTO.DailySalesStat> calculateDailySalesStats(List<CustomerOrder> orders, LocalDate startDate, LocalDate endDate) {
        Map<String, List<CustomerOrder>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(order -> {
                    LocalDate orderDate = order.getCreateTime().toLocalDate();
                    return orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }));

        List<SalesReportDTO.DailySalesStat> stats = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<CustomerOrder> dayOrders = ordersByDate.getOrDefault(dateStr, Collections.emptyList());

            SalesReportDTO.DailySalesStat stat = new SalesReportDTO.DailySalesStat();
            stat.setDate(dateStr);
            stat.setOrderCount((long) dayOrders.size());

            BigDecimal totalAmount = dayOrders.stream()
                    .map(order -> order.getActualAmount() != null ? order.getActualAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stat.setTotalAmount(totalAmount);

            if (dayOrders.size() > 0) {
                stat.setAvgAmount(totalAmount.divide(BigDecimal.valueOf(dayOrders.size()), 2, RoundingMode.HALF_UP));
            } else {
                stat.setAvgAmount(BigDecimal.ZERO);
            }

            stats.add(stat);
            currentDate = currentDate.plusDays(1);
        }

        return stats;
    }

    /**
     * 计算菜品销售排行
     */
    private List<SalesReportDTO.DishSalesRank> calculateDishSalesRanks(List<CustomerOrder> orders) {
        // 获取所有订单的订单ID
        List<Long> orderIds = orders.stream()
                .map(CustomerOrder::getOrderId)
                .collect(Collectors.toList());

        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询所有订单详情
        LambdaQueryWrapper<OrderDetail> detailWrapper = new QueryWrapper<OrderDetail>().lambda();
        detailWrapper.in(OrderDetail::getOrderId, orderIds);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailWrapper);

        // 按菜品名称分组统计
        Map<String, DishStats> dishStatsMap = new HashMap<>();
        for (OrderDetail detail : orderDetails) {
            String dishName = detail.getItemName();
            DishStats stats = dishStatsMap.getOrDefault(dishName, new DishStats());
            stats.salesCount += detail.getQuantity();
            stats.salesAmount = stats.salesAmount.add(
                    detail.getSubtotal() != null ? detail.getSubtotal() : BigDecimal.ZERO
            );
            dishStatsMap.put(dishName, stats);
        }

        // 转换为DTO并排序
        List<SalesReportDTO.DishSalesRank> ranks = dishStatsMap.entrySet().stream()
                .map(entry -> {
                    SalesReportDTO.DishSalesRank rank = new SalesReportDTO.DishSalesRank();
                    rank.setDishName(entry.getKey());
                    rank.setSalesCount(entry.getValue().salesCount);
                    rank.setSalesAmount(entry.getValue().salesAmount);
                    return rank;
                })
                .sorted((a, b) -> Long.compare(b.getSalesCount(), a.getSalesCount()))
                .limit(10) // 取前10名
                .collect(Collectors.toList());

        return ranks;
    }

    /**
     * 计算时段分析
     */
    private List<SalesReportDTO.TimeAnalysis> calculateTimeAnalysis(List<CustomerOrder> orders) {
        // 按小时分组统计订单数
        Map<Integer, Long> ordersByHour = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreateTime().getHour(),
                        Collectors.counting()
                ));

        // 生成所有时段（11:00-21:00）
        List<SalesReportDTO.TimeAnalysis> analyses = new ArrayList<>();
        for (int hour = 11; hour <= 21; hour++) {
            SalesReportDTO.TimeAnalysis analysis = new SalesReportDTO.TimeAnalysis();
            analysis.setTimeSlot(String.format("%02d:00", hour));
            analysis.setOrderCount(ordersByHour.getOrDefault(hour, 0L));
            analyses.add(analysis);
        }

        return analyses;
    }

    /**
     * 计算销售明细
     */
    private List<SalesReportDTO.SalesDetail> calculateSalesDetails(List<CustomerOrder> orders, LocalDate startDate, LocalDate endDate) {
        Map<String, List<CustomerOrder>> ordersByDate = orders.stream()
                .collect(Collectors.groupingBy(order -> {
                    LocalDate orderDate = order.getCreateTime().toLocalDate();
                    return orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }));

        // 获取所有订单的订单ID，用于查询订单详情
        List<Long> allOrderIds = orders.stream()
                .map(CustomerOrder::getOrderId)
                .collect(Collectors.toList());

        Map<Long, String> topDishMap = new HashMap<>();
        if (!allOrderIds.isEmpty()) {
            LambdaQueryWrapper<OrderDetail> detailWrapper = new QueryWrapper<OrderDetail>().lambda();
            detailWrapper.in(OrderDetail::getOrderId, allOrderIds);
            List<OrderDetail> allDetails = orderDetailMapper.selectList(detailWrapper);

            // 按订单ID分组，找出每个订单销量最高的菜品
            Map<Long, Map<String, Long>> orderDishCounts = allDetails.stream()
                    .collect(Collectors.groupingBy(
                            OrderDetail::getOrderId,
                            Collectors.groupingBy(
                                    OrderDetail::getItemName,
                                    Collectors.summingLong(OrderDetail::getQuantity)
                            )
                    ));

            for (Map.Entry<Long, Map<String, Long>> entry : orderDishCounts.entrySet()) {
                String topDish = entry.getValue().entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("");
                topDishMap.put(entry.getKey(), topDish);
            }
        }

        List<SalesReportDTO.SalesDetail> details = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<CustomerOrder> dayOrders = ordersByDate.getOrDefault(dateStr, Collections.emptyList());

            SalesReportDTO.SalesDetail detail = new SalesReportDTO.SalesDetail();
            detail.setDate(dateStr);
            detail.setOrderCount((long) dayOrders.size());

            BigDecimal totalAmount = dayOrders.stream()
                    .map(order -> order.getActualAmount() != null ? order.getActualAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            detail.setTotalAmount(totalAmount);

            if (dayOrders.size() > 0) {
                detail.setAvgAmount(totalAmount.divide(BigDecimal.valueOf(dayOrders.size()), 2, RoundingMode.HALF_UP));
            } else {
                detail.setAvgAmount(BigDecimal.ZERO);
            }

            // 找出当天销量最高的菜品
            if (!dayOrders.isEmpty()) {
                Map<String, Long> dayDishCounts = new HashMap<>();
                for (CustomerOrder order : dayOrders) {
                    String topDish = topDishMap.get(order.getOrderId());
                    if (topDish != null) {
                        dayDishCounts.put(topDish, dayDishCounts.getOrDefault(topDish, 0L) + 1);
                    }
                }
                String topDish = dayDishCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("-");
                detail.setTopDish(topDish);
            } else {
                detail.setTopDish("-");
            }

            details.add(detail);
            currentDate = currentDate.plusDays(1);
        }

        return details;
    }

    /**
     * 菜品统计辅助类
     */
    private static class DishStats {
        long salesCount = 0;
        BigDecimal salesAmount = BigDecimal.ZERO;
    }

    @Override
    public CustomerReportDTO getCustomerReport(LocalDate startDate, LocalDate endDate) {
        CustomerReportDTO report = new CustomerReportDTO();

        // 如果未指定日期范围，默认查询最近30天
        if (startDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(29);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // 查询所有客户（userType=2）
        LambdaQueryWrapper<SysUser> userWrapper = new QueryWrapper<SysUser>().lambda();
        userWrapper.eq(SysUser::getUserType, 2);
        List<SysUser> allUsers = userService.list(userWrapper);

        // 1. 新客户数（指定日期范围内注册的客户）
        LambdaQueryWrapper<SysUser> newUserWrapper = new QueryWrapper<SysUser>().lambda();
        newUserWrapper.eq(SysUser::getUserType, 2);
        newUserWrapper.ge(SysUser::getCreateTime, startDateTime);
        newUserWrapper.lt(SysUser::getCreateTime, endDateTime);
        Long newCustomers = userService.count(newUserWrapper);
        report.setNewCustomers(newCustomers != null ? newCustomers : 0L);

        // 2. 老客户数（指定日期范围之前注册的客户）
        LambdaQueryWrapper<SysUser> oldUserWrapper = new QueryWrapper<SysUser>().lambda();
        oldUserWrapper.eq(SysUser::getUserType, 2);
        oldUserWrapper.lt(SysUser::getCreateTime, startDateTime);
        Long oldCustomers = userService.count(oldUserWrapper);
        report.setOldCustomers(oldCustomers != null ? oldCustomers : 0L);

        // 3. 新老客户比例
        long totalCustomers = newCustomers + oldCustomers;
        if (totalCustomers > 0) {
            report.setCustomerRatio((double) newCustomers / totalCustomers * 100);
        } else {
            report.setCustomerRatio(0.0);
        }

        // 4. 平均消费频次（查询指定日期范围内的订单，统计每个客户的订单数）
        LambdaQueryWrapper<CustomerOrder> orderWrapper = new QueryWrapper<CustomerOrder>().lambda();
        orderWrapper.ge(CustomerOrder::getCreateTime, startDateTime);
        orderWrapper.lt(CustomerOrder::getCreateTime, endDateTime);
        orderWrapper.eq(CustomerOrder::getStatus, 1); // 只统计已支付的订单
        List<CustomerOrder> orders = orderService.list(orderWrapper);

        Map<Long, Long> orderCountByUser = orders.stream()
                .collect(Collectors.groupingBy(
                        CustomerOrder::getUserId,
                        Collectors.counting()
                ));

        if (!orderCountByUser.isEmpty()) {
            double totalFrequency = orderCountByUser.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();
            report.setAvgFrequency(totalFrequency / orderCountByUser.size());
        } else {
            report.setAvgFrequency(0.0);
        }

        // 5. 消费频次分布
        List<CustomerReportDTO.FrequencyDistribution> frequencyDistributions = new ArrayList<>();
        
        // 统计各频次区间的客户数
        long count1 = orderCountByUser.values().stream().filter(c -> c == 1).count();
        long count2to3 = orderCountByUser.values().stream().filter(c -> c >= 2 && c <= 3).count();
        long count4to5 = orderCountByUser.values().stream().filter(c -> c >= 4 && c <= 5).count();
        long count6to10 = orderCountByUser.values().stream().filter(c -> c >= 6 && c <= 10).count();
        long count10plus = orderCountByUser.values().stream().filter(c -> c > 10).count();

        frequencyDistributions.add(createFrequencyDistribution("1次", count1));
        frequencyDistributions.add(createFrequencyDistribution("2-3次", count2to3));
        frequencyDistributions.add(createFrequencyDistribution("4-5次", count4to5));
        frequencyDistributions.add(createFrequencyDistribution("6-10次", count6to10));
        frequencyDistributions.add(createFrequencyDistribution("10次以上", count10plus));
        
        report.setFrequencyDistributions(frequencyDistributions);

        // 6. 客户价值分析（按VIP等级分组）
        List<CustomerReportDTO.CustomerValue> customerValues = new ArrayList<>();
        
        // 按VIP等级分组
        Map<Integer, List<SysUser>> usersByVipLevel = allUsers.stream()
                .collect(Collectors.groupingBy(SysUser::getVipLevel));

        // 获取所有客户的订单数据（用于计算平均消费）
        Map<Long, List<CustomerOrder>> ordersByUser = orders.stream()
                .collect(Collectors.groupingBy(CustomerOrder::getUserId));

        for (Map.Entry<Integer, List<SysUser>> entry : usersByVipLevel.entrySet()) {
            Integer vipLevel = entry.getKey();
            List<SysUser> users = entry.getValue();
            
            CustomerReportDTO.CustomerValue value = new CustomerReportDTO.CustomerValue();
            value.setLevel(getVipLevelName(vipLevel));
            value.setCount((long) users.size());

            // 计算该等级客户的平均消费和总消费
            BigDecimal totalConsumption = BigDecimal.ZERO;
            int orderCount = 0;
            for (SysUser user : users) {
                if (user.getTotalConsumption() != null) {
                    totalConsumption = totalConsumption.add(user.getTotalConsumption());
                }
                List<CustomerOrder> userOrders = ordersByUser.getOrDefault(user.getUserId(), Collections.emptyList());
                orderCount += userOrders.size();
            }

            if (users.size() > 0) {
                value.setAvgConsumption(totalConsumption.divide(BigDecimal.valueOf(users.size()), 2, RoundingMode.HALF_UP));
            } else {
                value.setAvgConsumption(BigDecimal.ZERO);
            }
            value.setTotalConsumption(totalConsumption);

            if (allUsers.size() > 0) {
                value.setPercentage((double) users.size() / allUsers.size() * 100);
            } else {
                value.setPercentage(0.0);
            }

            customerValues.add(value);
        }

        // 按客户数量排序
        customerValues.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        report.setCustomerValues(customerValues);

        return report;
    }

    /**
     * 创建消费频次分布
     */
    private CustomerReportDTO.FrequencyDistribution createFrequencyDistribution(String range, long count) {
        CustomerReportDTO.FrequencyDistribution dist = new CustomerReportDTO.FrequencyDistribution();
        dist.setRange(range);
        dist.setCustomerCount(count);
        return dist;
    }

    /**
     * 获取VIP等级名称
     */
    private String getVipLevelName(Integer vipLevel) {
        if (vipLevel == null) {
            return "普通客户";
        }
        switch (vipLevel) {
            case 0:
                return "普通客户";
            case 1:
                return "VIP1客户";
            case 2:
                return "VIP2客户";
            case 3:
                return "VIP3客户";
            default:
                return "普通客户";
        }
    }

    @Override
    public InventoryReportDTO getInventoryReport() {
        InventoryReportDTO report = new InventoryReportDTO();

        // 查询所有上架的菜品
        LambdaQueryWrapper<MenuItem> itemWrapper = new QueryWrapper<MenuItem>().lambda();
        itemWrapper.eq(MenuItem::getStatus, 1); // 只查询上架的菜品
        List<MenuItem> items = menuItemService.list(itemWrapper);

        // 1. 库存预警（库存小于50的菜品）
        int warningThreshold = 50; // 预警阈值：50
        List<InventoryReportDTO.InventoryWarning> warnings = new ArrayList<>();
        for (MenuItem item : items) {
            int currentStock = item.getStock() != null ? item.getStock() : 0;
            
            if (currentStock < warningThreshold) {
                InventoryReportDTO.InventoryWarning warning = new InventoryReportDTO.InventoryWarning();
                warning.setItemId(item.getItemId());
                warning.setDishName(item.getItemName());
                warning.setCurrentStock(currentStock);
                warning.setMinStock(warningThreshold);
                // 如果库存小于25（阈值的一半），标记为紧急；否则为警告
                warning.setStatus(currentStock < warningThreshold / 2 ? "紧急" : "警告");
                warnings.add(warning);
            }
        }
        report.setWarningCount((long) warnings.size());
        report.setWarnings(warnings);

        // 2. 菜品消耗统计（最近7天）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // 查询最近7天的已支付订单
        LambdaQueryWrapper<CustomerOrder> orderWrapper = new QueryWrapper<CustomerOrder>().lambda();
        orderWrapper.ge(CustomerOrder::getCreateTime, startDateTime);
        orderWrapper.lt(CustomerOrder::getCreateTime, endDateTime);
        orderWrapper.eq(CustomerOrder::getStatus, 1);
        List<CustomerOrder> orders = orderService.list(orderWrapper);

        // 获取所有订单详情
        List<Long> orderIds = orders.stream()
                .map(CustomerOrder::getOrderId)
                .collect(Collectors.toList());

        Map<String, Integer> dailyConsumptionMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<OrderDetail> detailWrapper = new QueryWrapper<OrderDetail>().lambda();
            detailWrapper.in(OrderDetail::getOrderId, orderIds);
            List<OrderDetail> details = orderDetailMapper.selectList(detailWrapper);

            // 按日期分组统计消耗量
            for (OrderDetail detail : details) {
                // 通过订单ID找到订单，获取创建日期
                CustomerOrder order = orders.stream()
                        .filter(o -> o.getOrderId().equals(detail.getOrderId()))
                        .findFirst()
                        .orElse(null);
                if (order != null) {
                    String dateStr = order.getCreateTime().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    int quantity = detail.getQuantity() != null ? detail.getQuantity() : 0;
                    dailyConsumptionMap.put(dateStr, dailyConsumptionMap.getOrDefault(dateStr, 0) + quantity);
                }
            }
        }

        // 生成最近7天的消耗数据
        List<InventoryReportDTO.DailyConsumption> dailyConsumptions = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            InventoryReportDTO.DailyConsumption consumption = new InventoryReportDTO.DailyConsumption();
            consumption.setDate(dateStr);
            consumption.setConsumption(dailyConsumptionMap.getOrDefault(dateStr, 0));
            dailyConsumptions.add(consumption);
            currentDate = currentDate.plusDays(1);
        }
        report.setDailyConsumptions(dailyConsumptions);

        // 3. 采购建议
        List<InventoryReportDTO.PurchaseSuggestion> purchaseSuggestions = new ArrayList<>();
        
        // 计算每个菜品的日均消耗
        Map<Long, Integer> itemConsumptionMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            LambdaQueryWrapper<OrderDetail> detailWrapper = new QueryWrapper<OrderDetail>().lambda();
            detailWrapper.in(OrderDetail::getOrderId, orderIds);
            List<OrderDetail> allDetails = orderDetailMapper.selectList(detailWrapper);

            for (OrderDetail detail : allDetails) {
                Long itemId = detail.getItemId();
                int quantity = detail.getQuantity() != null ? detail.getQuantity() : 0;
                itemConsumptionMap.put(itemId, itemConsumptionMap.getOrDefault(itemId, 0) + quantity);
            }
        }

        for (MenuItem item : items) {
            int currentStock = item.getStock() != null ? item.getStock() : 0;
            int totalConsumption = itemConsumptionMap.getOrDefault(item.getItemId(), 0);
            // 计算日均消耗（向上取整，因为菜品是按份计算的）
            int avgDailyConsumption = totalConsumption > 0 ? (int) Math.ceil(totalConsumption / 7.0) : 0;

            if (avgDailyConsumption > 0) {
                InventoryReportDTO.PurchaseSuggestion suggestion = new InventoryReportDTO.PurchaseSuggestion();
                suggestion.setItemId(item.getItemId());
                suggestion.setDishName(item.getItemName());
                suggestion.setCurrentStock(currentStock);
                suggestion.setAvgDailyConsumption(avgDailyConsumption * 1.0); // 转换为Double类型

                // 建议采购量：保证至少7天的库存
                int suggestedPurchase = avgDailyConsumption * 7;
                suggestion.setSuggestedPurchase(suggestedPurchase);

                // 预计可用天数（向下取整）
                suggestion.setEstimatedDays(currentStock / avgDailyConsumption);

                purchaseSuggestions.add(suggestion);
            }
        }

        // 按预计可用天数排序（天数少的优先）
        purchaseSuggestions.sort((a, b) -> Integer.compare(a.getEstimatedDays(), b.getEstimatedDays()));
        report.setPurchaseSuggestions(purchaseSuggestions);

        return report;
    }
}

