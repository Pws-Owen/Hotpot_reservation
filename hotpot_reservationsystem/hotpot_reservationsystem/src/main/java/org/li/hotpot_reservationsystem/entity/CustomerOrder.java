package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@Data
@TableName("customer_order")
public class CustomerOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 预约ID（如果是预约点餐）
     */
    @TableField("reservation_id")
    private Long reservationId;

    /**
     * 餐桌ID
     */
    @TableField("table_id")
    private Long tableId;

    /**
     * 订单总价
     */
    @TableField("total_price")
    private BigDecimal totalPrice;

    /**
     * 优惠金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    /**
     * 订单类型：1-堂食，2-外卖，3-预约点餐
     */
    @TableField("order_type")
    private Integer orderType;

    /**
     * 订单状态：0-待支付，1-已支付，2-制作中，3-已完成，4-已取消，5-已退款
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 完成时间
     */
    @TableField("finish_time")
    private LocalDateTime finishTime;
}

