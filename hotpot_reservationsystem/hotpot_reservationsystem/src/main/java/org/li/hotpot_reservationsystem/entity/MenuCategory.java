package org.li.hotpot_reservationsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单分类表
 */
@Data
@TableName("menu_category")
public class MenuCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    /**
     * 分类名称
     */
    @TableField("category_name")
    private String categoryName;

    /**
     * 分类编码
     */
    @TableField("category_code")
    private String categoryCode;

    /**
     * 父分类ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 分类描述
     */
    @TableField("description")
    private String description;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
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

