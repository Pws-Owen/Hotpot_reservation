package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.Reservation;
import org.li.hotpot_reservationsystem.entity.RestaurantTable;
import org.li.hotpot_reservationsystem.mapper.ReservationMapper;
import org.li.hotpot_reservationsystem.utils.UniqueNoGenerator;
import org.li.hotpot_reservationsystem.service.ReservationService;
import org.li.hotpot_reservationsystem.service.RestaurantTableService;
import org.li.hotpot_reservationsystem.utils.RetryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约Service实现类
 */
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private RestaurantTableService tableService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation createReservation(Reservation reservation) {
        // 生成唯一的预约单号（带重试机制，防止并发冲突）
        reservation.setStatus(0); // 待确认
        RetryUtil.executeWithRetry(() -> {
            String reservationNo = generateUniqueReservationNo();
            reservation.setReservationNo(reservationNo);
            this.save(reservation);
            return reservation;
        }, "生成预约单号失败，请稍后重试");
        return reservation;
    }
    
    /**
     * 生成唯一的预约单号
     * 格式：RES + yyyyMMdd + 4位序号（从数据库中查询当天最大序号+1）
     */
    private String generateUniqueReservationNo() {
        String prefix = UniqueNoGenerator.buildFullPrefix("RES");
        
        // 查询当天最大的预约单号
        LambdaQueryWrapper<Reservation> wrapper = new QueryWrapper<Reservation>().lambda();
        wrapper.likeRight(Reservation::getReservationNo, prefix);
        wrapper.orderByDesc(Reservation::getReservationNo);
        wrapper.last("LIMIT 1");
        
        Reservation lastReservation = this.getOne(wrapper);
        String lastNo = lastReservation != null ? lastReservation.getReservationNo() : null;
        
        return UniqueNoGenerator.generateNo("RES", lastNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReservation(Long reservationId, Long confirmBy) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        if (reservation.getStatus() != 0) {
            throw new RuntimeException("预约状态错误，无法确认");
        }
        reservation.setStatus(1); // 已确认
        reservation.setConfirmBy(confirmBy);
        reservation.setConfirmTime(LocalDateTime.now());
        this.updateById(reservation);
        
        // 如果预约指定了餐桌，将餐桌状态改为已预订
        if (reservation.getTableId() != null) {
            RestaurantTable table = tableService.getById(reservation.getTableId());
            if (table != null) {
                // 检查餐桌状态，只有可用状态的餐桌才能被预订
                if (table.getStatus() == 1) { // 1-可用
                    table.setStatus(2); // 2-已预订
                    tableService.updateById(table);
                } else if (table.getStatus() != 2) {
                    // 如果餐桌不是可用状态，抛出异常
                    throw new RuntimeException("餐桌状态错误，无法预订。当前状态：" + getTableStatusText(table.getStatus()));
                }
            } else {
                throw new RuntimeException("餐桌不存在");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Long reservationId, String cancelReason) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        
        // 记录预约的原始状态和餐桌ID
        Integer originalStatus = reservation.getStatus();
        Long tableId = reservation.getTableId();
        
        reservation.setStatus(4); // 已取消
        reservation.setCancelReason(cancelReason);
        this.updateById(reservation);
        
        // 如果预约已确认（status=1）且指定了餐桌，将餐桌状态改回可用
        if (originalStatus == 1 && tableId != null) {
            RestaurantTable table = tableService.getById(tableId);
            if (table != null && table.getStatus() == 2) { // 2-已预订
                table.setStatus(1); // 1-可用
                tableService.updateById(table);
            }
        }
    }
    
    /**
     * 获取餐桌状态文本（用于错误提示）
     */
    private String getTableStatusText(Integer status) {
        switch (status) {
            case 0: return "禁用";
            case 1: return "可用";
            case 2: return "已预订";
            case 3: return "使用中";
            case 4: return "维修中";
            default: return "未知";
        }
    }

    @Override
    public PageResult<Reservation> getReservationPage(Long current, Long size, Integer status, Long userId, String date) {
        Page<Reservation> page = new Page<>(current, size);
        LambdaQueryWrapper<Reservation> wrapper = new QueryWrapper<Reservation>().lambda();
        
        if (status != null) {
            wrapper.eq(Reservation::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(Reservation::getUserId, userId);
        }
        if (date != null && !date.isEmpty()) {
            LocalDate localDate = LocalDate.parse(date);
            wrapper.eq(Reservation::getReservationDate, localDate);
        }
        
        wrapper.orderByDesc(Reservation::getCreateTime);
        Page<Reservation> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }
}

