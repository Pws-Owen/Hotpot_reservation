package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.dto.CreateOrderRequest;
import org.li.hotpot_reservationsystem.entity.CustomerOrder;
import org.li.hotpot_reservationsystem.entity.OrderDetail;
import org.li.hotpot_reservationsystem.service.CustomerOrderService;
import org.li.hotpot_reservationsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单Controller
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private CustomerOrderService orderService;

    @Autowired
    private org.li.hotpot_reservationsystem.service.SysUserService userService;

    /**
     * 创建订单（包含订单详情）
     */
    @PostMapping
    public Result<CustomerOrder> createOrder(@RequestBody CreateOrderRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        CustomerOrder result = orderService.createOrder(request, userId);
        return Result.success(result);
    }

    /**
     * 分页查询订单列表
     */
    @GetMapping("/page")
    public Result<PageResult<? extends CustomerOrder>> getOrderPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String date) {
        LocalDate localDate = date != null ? LocalDate.parse(date) : null;
        
        // 检查用户角色，如果不是管理员，只能查看自己的订单
        if (SecurityUtil.isAuthenticated()) {
            try {
                Long currentUserId = SecurityUtil.getCurrentUserId();
                
                // 获取当前用户的角色编码列表
                List<String> roleCodes = userService.getRoleCodesByUserId(currentUserId);
                
                // 判断是否是管理员（ADMIN）或前台服务员（RECEPTIONIST）
                boolean isAdmin = roleCodes != null && (roleCodes.contains("ADMIN") || roleCodes.contains("RECEPTIONIST"));
                
                if (!isAdmin) {
                    // 普通用户只能查看自己的订单，强制设置为当前用户ID
                    userId = currentUserId;
                }
                // 如果是管理员或前台，userId 可以为 null（查看所有订单）或指定 userId（查看特定用户的订单）
            } catch (Exception e) {
                // 如果无法获取用户ID，返回错误
                throw new RuntimeException("无法获取用户信息", e);
            }
        } else {
            // 未登录用户，返回错误
            throw new RuntimeException("用户未登录");
        }
        
        PageResult<? extends CustomerOrder> result = orderService.getOrderPage(current, size, status, userId, localDate);
        return Result.success(result);
    }

    /**
     * 获取订单详情（包含订单详情列表）
     */
    @GetMapping("/{id}")
    public Result<org.li.hotpot_reservationsystem.dto.OrderDetailDTO> getOrderById(@PathVariable Long id) {
        CustomerOrder order = orderService.getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 获取订单详情列表
        List<OrderDetail> details = orderService.getOrderDetails(id);
        
        // 构建返回对象
        org.li.hotpot_reservationsystem.dto.OrderDetailDTO dto = new org.li.hotpot_reservationsystem.dto.OrderDetailDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setReservationId(order.getReservationId());
        dto.setTableId(order.getTableId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setActualAmount(order.getActualAmount());
        dto.setOrderType(order.getOrderType());
        dto.setStatus(order.getStatus());
        dto.setRemark(order.getRemark());
        dto.setCreateTime(order.getCreateTime());
        dto.setUpdateTime(order.getUpdateTime());
        dto.setPayTime(order.getPayTime());
        dto.setFinishTime(order.getFinishTime());
        dto.setDetails(details);
        
        return Result.success(dto);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id, @RequestBody(required = false) CancelOrderRequest request) {
        try {
            String reason = request != null ? request.getReason() : null;
            orderService.cancelOrder(id, reason);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id, @RequestBody PayOrderRequest request) {
        try {
            String paymentMethod = request != null ? request.getPaymentMethod() : "ALIPAY";
            orderService.payOrder(id, paymentMethod);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新订单状态（管理端使用）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        try {
            orderService.updateOrderStatus(id, request.getStatus());
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取订单详情列表
     */
    @GetMapping("/{id}/details")
    public Result<List<OrderDetail>> getOrderDetails(@PathVariable Long id) {
        try {
            List<OrderDetail> details = orderService.getOrderDetails(id);
            return Result.success(details);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消订单请求DTO
     */
    public static class CancelOrderRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    /**
     * 支付订单请求DTO
     */
    public static class PayOrderRequest {
        private String paymentMethod;

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }
    }

    /**
     * 更新订单状态请求DTO
     */
    public static class UpdateStatusRequest {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}

