package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.SysLog;
import org.li.hotpot_reservationsystem.mapper.SysLogMapper;
import org.li.hotpot_reservationsystem.service.SysLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 操作日志Service实现类
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    @Override
    public PageResult<SysLog> getLogPage(Long current, Long size, String username, String operation, Integer status, LocalDate startDate, LocalDate endDate) {
        Page<SysLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SysLog> wrapper = new QueryWrapper<SysLog>().lambda();
        
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysLog::getUsername, username);
        }
        if (operation != null && !operation.isEmpty()) {
            wrapper.like(SysLog::getOperation, operation);
        }
        if (status != null) {
            wrapper.eq(SysLog::getStatus, status);
        }
        if (startDate != null) {
            wrapper.ge(SysLog::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.lt(SysLog::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }
        
        wrapper.orderByDesc(SysLog::getCreateTime);
        Page<SysLog> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }
}

