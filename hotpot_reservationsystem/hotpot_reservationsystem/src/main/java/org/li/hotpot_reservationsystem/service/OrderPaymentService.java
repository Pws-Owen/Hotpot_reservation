package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.OrderPayment;

import java.math.BigDecimal;

/**
 * 支付记录Service接口
 */
public interface OrderPaymentService extends IService<OrderPayment> {
    /**
     * 创建支付记录（模拟支付）
     */
    OrderPayment createPayment(Long orderId, String paymentMethod, BigDecimal amount);

    /**
     * 模拟支付处理
     */
    void processPayment(Long orderId, String paymentMethod);

    /**
     * 分页查询支付记录
     */
    PageResult<OrderPayment> getPaymentPage(Long current, Long size, Long orderId, Integer status);

    /**
     * 退款
     * 
     * @param paymentId 支付记录ID
     * @param reason 退款原因
     */
    void refund(Long paymentId, String reason);
}

