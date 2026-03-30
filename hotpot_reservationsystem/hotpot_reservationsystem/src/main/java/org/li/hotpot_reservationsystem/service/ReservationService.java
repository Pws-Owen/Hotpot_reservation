package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.Reservation;

/**
 * 预约Service接口
 */
public interface ReservationService extends IService<Reservation> {
    /**
     * 创建预约
     */
    Reservation createReservation(Reservation reservation);

    /**
     * 确认预约
     */
    void confirmReservation(Long reservationId, Long confirmBy);

    /**
     * 取消预约
     */
    void cancelReservation(Long reservationId, String cancelReason);

    /**
     * 分页查询预约列表
     */
    PageResult<Reservation> getReservationPage(Long current, Long size, Integer status, Long userId, String date);
}

