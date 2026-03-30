package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 库存报表DTO
 */
@Data
public class InventoryReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 库存预警数量
     */
    private Long warningCount;

    /**
     * 库存预警列表
     */
    private List<InventoryWarning> warnings;

    /**
     * 菜品消耗统计（最近7天）
     */
    private List<DailyConsumption> dailyConsumptions;

    /**
     * 采购建议
     */
    private List<PurchaseSuggestion> purchaseSuggestions;

    /**
     * 库存预警
     */
    @Data
    public static class InventoryWarning implements Serializable {
        private Long itemId;
        private String dishName;
        private Integer currentStock;
        private Integer minStock;
        private String status; // 紧急/警告
    }

    /**
     * 每日消耗
     */
    @Data
    public static class DailyConsumption implements Serializable {
        private String date;
        private Integer consumption;
    }

    /**
     * 采购建议
     */
    @Data
    public static class PurchaseSuggestion implements Serializable {
        private Long itemId;
        private String dishName;
        private Integer currentStock;
        private Double avgDailyConsumption;
        private Integer suggestedPurchase;
        private Integer estimatedDays;
    }
}

