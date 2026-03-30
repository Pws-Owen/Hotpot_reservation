package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论DTO（包含用户信息）
 */
@Data
public class ReviewDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long reviewId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 菜品ID
     */
    private Long itemId;

    /**
     * 评分：1-5分
     */
    private Integer rating;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片（多个用逗号分隔）
     */
    private String images;

    /**
     * 商家回复
     */
    private String reply;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 状态：0-隐藏，1-显示
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

