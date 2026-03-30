package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.dto.CreateOrderRequest;
import org.li.hotpot_reservationsystem.entity.CustomerOrder;
import org.li.hotpot_reservationsystem.entity.OrderDetail;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单Service接口
 */
public interface CustomerOrderService extends IService<CustomerOrder> {
    /**
     * 分页查询订单列表
     */
    PageResult<? extends CustomerOrder> getOrderPage(Long current, Long size, Integer status, Long userId, LocalDate date);

    /**
     * 创建订单（包含订单详情）
     */
    CustomerOrder createOrder(CreateOrderRequest request, Long userId);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, String reason);

    /**
     * 支付订单（模拟支付）
     */
    void payOrder(Long orderId, String paymentMethod);

    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long orderId, Integer status);

    /**
     * 获取订单详情列表
     */
    List<OrderDetail> getOrderDetails(Long orderId);
}

