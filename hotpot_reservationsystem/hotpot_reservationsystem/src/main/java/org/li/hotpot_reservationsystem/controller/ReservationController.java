package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.entity.Reservation;
import org.li.hotpot_reservationsystem.service.ReservationService;
import org.li.hotpot_reservationsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 预约Controller
 */
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    /**
     * 创建预约
     */
    @PostMapping
    public Result<Reservation> createReservation(@RequestBody Reservation reservation) {
        Long userId = SecurityUtil.getCurrentUserId();
        reservation.setUserId(userId);
        
        // 验证必填字段
        if (reservation.getReservationDate() == null) {
            throw new RuntimeException("预约日期不能为空");
        }
        if (reservation.getReservationTime() == null) {
            throw new RuntimeException("预约时间不能为空");
        }
        if (reservation.getGuestCount() == null || reservation.getGuestCount() <= 0) {
            throw new RuntimeException("用餐人数必须大于0");
        }
        if (reservation.getContactName() == null || reservation.getContactName().trim().isEmpty()) {
            throw new RuntimeException("联系人姓名不能为空");
        }
        if (reservation.getContactPhone() == null || reservation.getContactPhone().trim().isEmpty()) {
            throw new RuntimeException("联系电话不能为空");
        }
        
        Reservation result = reservationService.createReservation(reservation);
        return Result.success(result);
    }

    /**
     * 确认预约
     */
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmReservation(@PathVariable Long id) {
        Long confirmBy = SecurityUtil.getCurrentUserId();
        reservationService.confirmReservation(id, confirmBy);
        return Result.success();
    }

    /**
     * 取消预约
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long id, @RequestParam(required = false) String reason, @RequestBody(required = false) CancelReservationRequest request) {
        try {
            String cancelReason = reason != null ? reason : (request != null ? request.getReason() : null);
            reservationService.cancelReservation(id, cancelReason);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消预约请求DTO
     */
    public static class CancelReservationRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * 分页查询预约列表
     */
    @GetMapping("/page")
    public Result<PageResult<Reservation>> getReservationPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String date) {
        try {
            PageResult<Reservation> result = reservationService.getReservationPage(current, size, status, userId, date);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    public Result<Reservation> getReservationById(@PathVariable Long id) {
        try {
            Reservation reservation = reservationService.getById(id);
            if (reservation == null) {
                return Result.error("预约不存在");
            }
            return Result.success(reservation);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

