package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import org.li.hotpot_reservationsystem.entity.CustomerOrder;
import org.li.hotpot_reservationsystem.entity.OrderDetail;

import java.util.List;

/**
 * 订单详情DTO（包含订单详情列表）
 */
@Data
public class OrderDetailDTO extends CustomerOrder {
    /**
     * 订单详情列表
     */
    private List<OrderDetail> details;
}

