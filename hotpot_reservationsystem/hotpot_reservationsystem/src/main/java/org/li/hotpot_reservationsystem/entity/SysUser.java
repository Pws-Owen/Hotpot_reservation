package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码（加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 用户类型：1-系统用户，2-客户
     */
    @TableField("user_type")
    private Integer userType;

    /**
     * VIP等级：0-普通，1-VIP1，2-VIP2，3-VIP3
     */
    @TableField("vip_level")
    private Integer vipLevel;

    /**
     * 累计消费金额
     */
    @TableField("total_consumption")
    private java.math.BigDecimal totalConsumption;

    /**
     * 累计订单数
     */
    @TableField("total_orders")
    private Integer totalOrders;

    /**
     * 积分
     */
    @TableField("points")
    private Integer points;

    /**
     * 最后消费时间
     */
    @TableField("last_consume_time")
    private LocalDateTime lastConsumeTime;

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

