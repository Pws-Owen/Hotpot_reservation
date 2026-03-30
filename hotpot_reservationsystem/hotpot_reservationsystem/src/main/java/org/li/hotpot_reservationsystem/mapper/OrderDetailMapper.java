package org.li.hotpot_reservationsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.li.hotpot_reservationsystem.entity.OrderDetail;

/**
 * 订单详情Mapper接口
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}

