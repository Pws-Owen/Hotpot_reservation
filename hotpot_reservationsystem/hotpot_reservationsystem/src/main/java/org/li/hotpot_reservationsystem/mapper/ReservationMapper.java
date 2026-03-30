package org.li.hotpot_reservationsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.li.hotpot_reservationsystem.entity.Reservation;

/**
 * 预约Mapper接口
 */
@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {
}

