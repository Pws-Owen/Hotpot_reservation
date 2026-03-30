package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import org.li.hotpot_reservationsystem.entity.CustomerOrder;

/**
 * 订单DTO（包含桌子号、预约号和用户真实姓名）
 */
@Data
public class OrderWithTableReservationDTO extends CustomerOrder {
    /**
     * 桌子号
     */
    private String tableNumber;

    /**
     * 预约号
     */
    private String reservationNo;

    /**
     * 用户真实姓名
     */
    private String realName;
}

