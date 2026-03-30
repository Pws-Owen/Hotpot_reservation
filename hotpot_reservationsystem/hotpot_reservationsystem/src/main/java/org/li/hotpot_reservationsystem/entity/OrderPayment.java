package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表
 */
@Data
@TableName("order_payment")
public class OrderPayment implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 支付记录ID
     */
    @TableId(value = "payment_id", type = IdType.AUTO)
    private Long paymentId;

    /**
     * 支付单号
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 支付方式：ALIPAY-支付宝，WECHAT-微信，CASH-现金，CARD-银行卡
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款
     */
    @TableField("status")
    private Integer status;

    /**
     * 第三方交易号
     */
    @TableField("transaction_id")
    private String transactionId;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private LocalDateTime payTime;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    private LocalDateTime refundTime;

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
}

