package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 客户分析报表DTO
 */
@Data
public class CustomerReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 新客户数（指定日期范围内注册的客户）
     */
    private Long newCustomers;

    /**
     * 老客户数（指定日期范围之前注册的客户）
     */
    private Long oldCustomers;

    /**
     * 新老客户比例（新客户占比）
     */
    private Double customerRatio;

    /**
     * 平均消费频次
     */
    private Double avgFrequency;

    /**
     * 消费频次分布
     */
    private List<FrequencyDistribution> frequencyDistributions;

    /**
     * 客户价值分析
     */
    private List<CustomerValue> customerValues;

    /**
     * 消费频次分布
     */
    @Data
    public static class FrequencyDistribution implements Serializable {
        private String range;
        private Long customerCount;
    }

    /**
     * 客户价值
     */
    @Data
    public static class CustomerValue implements Serializable {
        private String level;
        private Long count;
        private BigDecimal avgConsumption;
        private BigDecimal totalConsumption;
        private Double percentage;
    }
}

