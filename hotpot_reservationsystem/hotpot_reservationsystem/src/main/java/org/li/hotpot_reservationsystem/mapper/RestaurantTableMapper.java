package org.li.hotpot_reservationsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.li.hotpot_reservationsystem.entity.RestaurantTable;

/**
 * 餐桌Mapper接口
 */
@Mapper
public interface RestaurantTableMapper extends BaseMapper<RestaurantTable> {
}

