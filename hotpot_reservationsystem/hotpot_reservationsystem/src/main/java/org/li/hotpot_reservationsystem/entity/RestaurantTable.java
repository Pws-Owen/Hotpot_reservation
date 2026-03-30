package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 餐桌表
 */
@Data
@TableName("restaurant_table")
public class RestaurantTable implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 餐桌ID
     */
    @TableId(value = "table_id", type = IdType.AUTO)
    private Long tableId;

    /**
     * 餐桌编号
     */
    @TableField("table_number")
    private String tableNumber;

    /**
     * 餐桌名称
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 容纳人数
     */
    @TableField("capacity")
    private Integer capacity;

    /**
     * 餐桌类型：NORMAL-普通，VIP-VIP，WINDOW-靠窗
     */
    @TableField("table_type")
    private String tableType;

    /**
     * 位置描述
     */
    @TableField("location")
    private String location;

    /**
     * 状态：0-禁用，1-可用，2-已预订，3-使用中，4-维修中
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
     * 描述
     */
    @TableField("description")
    private String description;

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

