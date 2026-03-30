package org.li.hotpot_reservationsystem.common;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 * 用于封装分页查询的结果，包含分页信息和数据列表
 * 
 * @param <T> 数据项类型
 */
@Data
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（从1开始）
     */
    private Long current;

    /**
     * 每页大小（每页记录数）
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数（自动计算：total/size向上取整）
     */
    private Long pages;

    /**
     * 当前页的数据列表
     */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long current, Long size, Long total, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = (total + size - 1) / size; // 计算总页数
        this.records = records;
    }

    /**
     * 从MyBatis-Plus的Page对象转换为PageResult
     * 
     * @param page MyBatis-Plus的分页对象
     * @param <T> 数据项类型
     * @return PageResult对象
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return new PageResult<>(
            page.getCurrent(),
            page.getSize(),
            page.getTotal(),
            page.getRecords()
        );
    }
}

