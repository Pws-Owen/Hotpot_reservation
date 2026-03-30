package org.li.hotpot_reservationsystem.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充处理器
 * 实现MetaObjectHandler接口，自动填充实体类的createTime和updateTime字段
 * 在插入记录时自动设置createTime和updateTime
 * 在更新记录时自动更新updateTime
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     * 自动填充createTime和updateTime字段为当前时间
     * 
     * @param metaObject 元对象（实体类的元数据）
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时自动填充
     * 自动更新updateTime字段为当前时间
     * 
     * @param metaObject 元对象（实体类的元数据）
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}

