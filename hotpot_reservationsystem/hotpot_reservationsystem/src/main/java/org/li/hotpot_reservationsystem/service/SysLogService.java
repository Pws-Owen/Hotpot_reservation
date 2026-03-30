package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.SysLog;

import java.time.LocalDate;

/**
 * 操作日志Service接口
 */
public interface SysLogService extends IService<SysLog> {
    /**
     * 分页查询操作日志列表
     */
    PageResult<SysLog> getLogPage(Long current, Long size, String username, String operation, Integer status, LocalDate startDate, LocalDate endDate);
}

