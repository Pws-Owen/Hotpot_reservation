package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.entity.OrderPayment;
import org.li.hotpot_reservationsystem.service.OrderPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 支付记录Controller
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private OrderPaymentService paymentService;

    /**
     * 分页查询支付记录
     */
    @GetMapping("/page")
    public Result<PageResult<OrderPayment>> getPaymentPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Integer status) {
        try {
            PageResult<OrderPayment> result = paymentService.getPaymentPage(current, size, orderId, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取支付记录详情
     */
    @GetMapping("/{id}")
    public Result<OrderPayment> getPaymentById(@PathVariable Long id) {
        OrderPayment payment = paymentService.getById(id);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }
        return Result.success(payment);
    }

    /**
     * 退款
     */
    @PostMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id, @RequestBody(required = false) RefundRequest request) {
        String reason = request != null ? request.getReason() : null;
        paymentService.refund(id, reason);
        return Result.success();
    }

    /**
     * 退款请求DTO
     */
    public static class RefundRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}

