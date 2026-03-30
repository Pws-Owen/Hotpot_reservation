package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志表
 */
@Data
@TableName("sys_log")
public class SysLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 操作
     */
    @TableField("operation")
    private String operation;

    /**
     * 请求方法
     */
    @TableField("method")
    private String method;

    /**
     * 请求参数
     */
    @TableField("params")
    private String params;

    /**
     * IP地址
     */
    @TableField("ip")
    private String ip;

    /**
     * 操作地点
     */
    @TableField("location")
    private String location;

    /**
     * 状态：0-失败，1-成功
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 执行时间（毫秒）
     */
    @TableField("execution_time")
    private Long executionTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

