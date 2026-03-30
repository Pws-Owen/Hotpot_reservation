package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.entity.SysLog;
import org.li.hotpot_reservationsystem.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 操作日志Controller
 */
@RestController
@RequestMapping("/log")
public class LogController {

    @Autowired
    private SysLogService logService;

    /**
     * 分页查询操作日志列表
     */
    @GetMapping("/page")
    public Result<PageResult<SysLog>> getLogPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;
            
            PageResult<SysLog> result = logService.getLogPage(current, size, username, operation, status, start, end);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取日志详情
     */
    @GetMapping("/{id}")
    public Result<SysLog> getLogById(@PathVariable Long id) {
        try {
            SysLog log = logService.getById(id);
            if (log == null) {
                return Result.error("日志不存在");
            }
            return Result.success(log);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除日志
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteLog(@PathVariable Long id) {
        try {
            logService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除日志
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteLogs(@RequestBody Long[] ids) {
        try {
            for (Long id : ids) {
                logService.removeById(id);
            }
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

