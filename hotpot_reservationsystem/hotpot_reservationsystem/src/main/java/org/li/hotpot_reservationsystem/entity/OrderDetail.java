package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情表
 */
@Data
@TableName("order_detail")
public class OrderDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单详情ID
     */
    @TableId(value = "detail_id", type = IdType.AUTO)
    private Long detailId;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 菜品ID
     */
    @TableField("item_id")
    private Long itemId;

    /**
     * 菜品名称（冗余字段）
     */
    @TableField("item_name")
    private String itemName;

    /**
     * 菜品图片（冗余字段）
     */
    @TableField("item_image")
    private String itemImage;

    /**
     * 单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 小计
     */
    @TableField("subtotal")
    private BigDecimal subtotal;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

