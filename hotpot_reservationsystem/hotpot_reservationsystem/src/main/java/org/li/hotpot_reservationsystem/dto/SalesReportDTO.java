package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 销售报表DTO
 */
@Data
public class SalesReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 每日销售统计
     */
    private List<DailySalesStat> dailySalesStats;

    /**
     * 菜品销售排行
     */
    private List<DishSalesRank> dishSalesRanks;

    /**
     * 时段分析
     */
    private List<TimeAnalysis> timeAnalyses;

    /**
     * 销售明细
     */
    private List<SalesDetail> salesDetails;

    /**
     * 每日销售统计
     */
    @Data
    public static class DailySalesStat implements Serializable {
        private String date;
        private BigDecimal totalAmount;
        private Long orderCount;
        private BigDecimal avgAmount;
    }

    /**
     * 菜品销售排行
     */
    @Data
    public static class DishSalesRank implements Serializable {
        private String dishName;
        private Long salesCount;
        private BigDecimal salesAmount;
    }

    /**
     * 时段分析
     */
    @Data
    public static class TimeAnalysis implements Serializable {
        private String timeSlot;
        private Long orderCount;
    }

    /**
     * 销售明细
     */
    @Data
    public static class SalesDetail implements Serializable {
        private String date;
        private BigDecimal totalAmount;
        private Long orderCount;
        private BigDecimal avgAmount;
        private String topDish;
    }
}

