package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜单表
 */
@Data
@TableName("menu_item")
public class MenuItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 菜品ID
     */
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    /**
     * 菜品名称
     */
    @TableField("item_name")
    private String itemName;

    /**
     * 菜品编码
     */
    @TableField("item_code")
    private String itemCode;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 菜品描述
     */
    @TableField("description")
    private String description;

    /**
     * 价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 图片URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 辣度：0-不辣，1-微辣，2-中辣，3-重辣
     */
    @TableField("spicy_level")
    private Integer spicyLevel;

    /**
     * 是否推荐：0-否，1-是
     */
    @TableField("is_recommend")
    private Integer isRecommend;

    /**
     * 是否热门：0-否，1-是
     */
    @TableField("is_hot")
    private Integer isHot;

    /**
     * 库存数量
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 销售数量
     */
    @TableField("sales_count")
    private Integer salesCount;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-下架，1-上架
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否删除：0-未删除，1-已删除（逻辑删除）
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

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

