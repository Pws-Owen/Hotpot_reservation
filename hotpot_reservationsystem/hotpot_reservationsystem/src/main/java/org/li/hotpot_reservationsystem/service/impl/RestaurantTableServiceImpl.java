package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.RestaurantTable;
import org.li.hotpot_reservationsystem.mapper.RestaurantTableMapper;
import org.li.hotpot_reservationsystem.service.RestaurantTableService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 餐桌Service实现类
 */
@Service
public class RestaurantTableServiceImpl extends ServiceImpl<RestaurantTableMapper, RestaurantTable> implements RestaurantTableService {

    @Override
    public PageResult<RestaurantTable> getTablePage(Long current, Long size, Integer status, String tableNumber) {
        Page<RestaurantTable> page = new Page<>(current, size);
        LambdaQueryWrapper<RestaurantTable> wrapper = new QueryWrapper<RestaurantTable>().lambda();
        
        if (status != null) {
            wrapper.eq(RestaurantTable::getStatus, status);
        }
        if (tableNumber != null && !tableNumber.isEmpty()) {
            wrapper.like(RestaurantTable::getTableNumber, tableNumber);
        }
        
        wrapper.orderByAsc(RestaurantTable::getTableNumber);
        Page<RestaurantTable> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    public List<RestaurantTable> getAvailableTables(LocalDate date, LocalTime time) {
        // 查询状态为可用(1)的餐桌
        LambdaQueryWrapper<RestaurantTable> wrapper = new QueryWrapper<RestaurantTable>().lambda();
        wrapper.eq(RestaurantTable::getStatus, 1); // 1-可用
        wrapper.orderByAsc(RestaurantTable::getTableNumber);
        return this.list(wrapper);
    }
}

