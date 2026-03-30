package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约表
 */
@Data
@TableName("reservation")
public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 预约ID
     */
    @TableId(value = "reservation_id", type = IdType.AUTO)
    private Long reservationId;

    /**
     * 预约单号
     */
    @TableField("reservation_no")
    private String reservationNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 餐桌ID
     */
    @TableField("table_id")
    private Long tableId;

    /**
     * 预约日期
     */
    @TableField("reservation_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    /**
     * 预约时间
     */
    @TableField("reservation_time")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime reservationTime;

    /**
     * 用餐时长（分钟）
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 用餐人数
     */
    @TableField("guest_count")
    private Integer guestCount;

    /**
     * 联系人姓名
     */
    @TableField("contact_name")
    private String contactName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 特殊要求
     */
    @TableField("special_request")
    private String specialRequest;

    /**
     * 状态：0-待确认，1-已确认，2-进行中，3-已完成，4-已取消，5-已过期
     */
    @TableField("status")
    private Integer status;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 确认人ID（前台服务员）
     */
    @TableField("confirm_by")
    private Long confirmBy;

    /**
     * 确认时间
     */
    @TableField("confirm_time")
    private LocalDateTime confirmTime;

    /**
     * 到店时间
     */
    @TableField("arrival_time")
    private LocalDateTime arrivalTime;

    /**
     * 完成时间
     */
    @TableField("finish_time")
    private LocalDateTime finishTime;

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

