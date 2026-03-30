package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.RestaurantTable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 餐桌Service接口
 */
public interface RestaurantTableService extends IService<RestaurantTable> {
    /**
     * 分页查询餐桌列表
     */
    PageResult<RestaurantTable> getTablePage(Long current, Long size, Integer status, String tableNumber);

    /**
     * 查询可用餐桌
     */
    List<RestaurantTable> getAvailableTables(LocalDate date, LocalTime time);
}

