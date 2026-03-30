package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论表
 */
@Data
@TableName("review")
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(value = "review_id", type = IdType.AUTO)
    private Long reviewId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 菜品ID（如果是对菜品的评论）
     */
    @TableField("item_id")
    private Long itemId;

    /**
     * 评分：1-5分
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 评论图片（多个用逗号分隔）
     */
    @TableField("images")
    private String images;

    /**
     * 商家回复
     */
    @TableField("reply")
    private String reply;

    /**
     * 回复时间
     */
    @TableField("reply_time")
    private LocalDateTime replyTime;

    /**
     * 状态：0-隐藏，1-显示
     */
    @TableField("status")
    private Integer status;

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

