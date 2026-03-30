package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.entity.RestaurantTable;
import org.li.hotpot_reservationsystem.service.RestaurantTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 餐桌Controller
 */
@RestController
@RequestMapping("/table")
public class TableController {

    @Autowired
    private RestaurantTableService tableService;

    /**
     * 获取所有餐桌
     */
    @GetMapping
    public Result<List<RestaurantTable>> getAllTables() {
        try {
            List<RestaurantTable> tables = tableService.list();
            return Result.success(tables);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询餐桌
     */
    @GetMapping("/page")
    public Result<PageResult<RestaurantTable>> getTablePage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String tableNumber) {
        try {
            PageResult<RestaurantTable> result = tableService.getTablePage(current, size, status, tableNumber);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取餐桌详情
     */
    @GetMapping("/{id}")
    public Result<RestaurantTable> getTableById(@PathVariable Long id) {
        RestaurantTable table = tableService.getById(id);
        if (table == null) {
            throw new RuntimeException("餐桌不存在");
        }
        return Result.success(table);
    }

    /**
     * 创建餐桌
     */
    @PostMapping
    public Result<RestaurantTable> createTable(@RequestBody RestaurantTable table) {
        // 检查桌号是否已存在
        RestaurantTable existing = tableService.lambdaQuery()
                .eq(RestaurantTable::getTableNumber, table.getTableNumber())
                .one();
        if (existing != null) {
            throw new RuntimeException("桌号已存在");
        }
        
        // 设置默认状态为可用
        if (table.getStatus() == null) {
            table.setStatus(1); // 1-可用
        }
        
        tableService.save(table);
        return Result.success(table);
    }

    /**
     * 更新餐桌
     */
    @PutMapping("/{id}")
    public Result<RestaurantTable> updateTable(@PathVariable Long id, @RequestBody RestaurantTable table) {
        try {
            RestaurantTable existing = tableService.getById(id);
            if (existing == null) {
                return Result.error("餐桌不存在");
            }
            
            // 如果修改了桌号，检查新桌号是否已存在
            if (!existing.getTableNumber().equals(table.getTableNumber())) {
                RestaurantTable duplicate = tableService.lambdaQuery()
                        .eq(RestaurantTable::getTableNumber, table.getTableNumber())
                        .ne(RestaurantTable::getTableId, id)
                        .one();
                if (duplicate != null) {
                    return Result.error("桌号已存在");
                }
            }
            
            table.setTableId(id);
            tableService.updateById(table);
            return Result.success(table);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除餐桌
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTable(@PathVariable Long id) {
        try {
            RestaurantTable table = tableService.getById(id);
            if (table == null) {
                return Result.error("餐桌不存在");
            }
            tableService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询可用餐桌
     */
    @GetMapping("/available")
    public Result<List<RestaurantTable>> getAvailableTables(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String time) {
        try {
            LocalDate localDate = date != null ? LocalDate.parse(date) : null;
            LocalTime localTime = time != null ? LocalTime.parse(time) : null;
            List<RestaurantTable> tables = tableService.getAvailableTables(localDate, localTime);
            return Result.success(tables);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

