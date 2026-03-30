package org.li.hotpot_reservationsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.li.hotpot_reservationsystem.entity.CustomerOrder;

/**
 * 订单Mapper接口
 */
@Mapper
public interface CustomerOrderMapper extends BaseMapper<CustomerOrder> {
}

