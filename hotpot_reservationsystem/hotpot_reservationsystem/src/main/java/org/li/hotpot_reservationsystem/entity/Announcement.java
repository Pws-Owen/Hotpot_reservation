package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告表
 */
@Data
@TableName("announcement")
public class Announcement implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    @TableId(value = "announcement_id", type = IdType.AUTO)
    private Long announcementId;

    /**
     * 公告标题
     */
    @TableField("title")
    private String title;

    /**
     * 公告内容
     */
    @TableField("content")
    private String content;

    /**
     * 公告类型：1-系统公告，2-活动公告，3-通知
     */
    @TableField("type")
    private Integer type;

    /**
     * 是否置顶：0-否，1-是
     */
    @TableField("is_top")
    private Integer isTop;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 发布时间
     * 注意：不使用@JsonFormat注解，使用JacksonConfig中的全局配置
     * 序列化格式：yyyy-MM-dd HH:mm:ss
     * 反序列化格式：支持 yyyy-MM-ddTHH:mm:ss (ISO格式，前端使用)
     */
    @TableField("publish_time")
    private LocalDateTime publishTime;

    /**
     * 创建人ID
     */
    @TableField("create_by")
    private Long createBy;

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

