package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
public class CreateOrderRequest {
    /**
     * 预约ID（可选，如果是预约点餐）
     */
    private Long reservationId;

    /**
     * 餐桌ID（可选）
     */
    private Long tableId;

    /**
     * 订单类型：1-堂食，2-外卖，3-预约点餐
     */
    private Integer orderType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单详情列表
     */
    private List<OrderDetailItem> items;

    /**
     * 订单详情项
     */
    @Data
    public static class OrderDetailItem {
        /**
         * 菜品ID
         */
        private Long itemId;

        /**
         * 数量
         */
        private Integer quantity;
    }
}

