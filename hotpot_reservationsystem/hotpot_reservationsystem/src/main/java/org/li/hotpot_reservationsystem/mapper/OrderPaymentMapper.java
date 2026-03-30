package org.li.hotpot_reservationsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.li.hotpot_reservationsystem.entity.OrderPayment;

/**
 * 支付记录Mapper接口
 */
@Mapper
public interface OrderPaymentMapper extends BaseMapper<OrderPayment> {
}

