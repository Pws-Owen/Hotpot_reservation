package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.CustomerOrder;
import org.li.hotpot_reservationsystem.entity.MenuItem;
import org.li.hotpot_reservationsystem.entity.OrderDetail;
import org.li.hotpot_reservationsystem.entity.OrderPayment;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.mapper.MenuItemMapper;
import org.li.hotpot_reservationsystem.mapper.OrderDetailMapper;
import org.li.hotpot_reservationsystem.mapper.OrderPaymentMapper;
import org.li.hotpot_reservationsystem.mapper.SysUserMapper;
import org.li.hotpot_reservationsystem.utils.UniqueNoGenerator;
import org.li.hotpot_reservationsystem.utils.RetryUtil;
import org.li.hotpot_reservationsystem.service.CustomerOrderService;
import org.li.hotpot_reservationsystem.service.OrderPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 支付记录Service实现类
 * 处理订单支付相关的业务逻辑，包括支付创建、支付处理、退款等
 */
@Service
public class OrderPaymentServiceImpl extends ServiceImpl<OrderPaymentMapper, OrderPayment> implements OrderPaymentService {

    @Autowired
    @org.springframework.context.annotation.Lazy
    private CustomerOrderService orderService;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private MenuItemMapper menuItemMapper;

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 创建支付记录
     * 
     * @param orderId 订单ID
     * @param paymentMethod 支付方式（如：WECHAT, ALIPAY等）
     * @param amount 支付金额
     * @return 创建的支付记录对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPayment createPayment(Long orderId, String paymentMethod, BigDecimal amount) {
        OrderPayment payment = new OrderPayment();
        payment.setOrderId(orderId);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setStatus(0); // 待支付
        payment.setTransactionId(UUID.randomUUID().toString().replace("-", ""));
        
        // 生成唯一的支付单号（带重试机制，防止并发冲突）
        RetryUtil.executeWithRetry(() -> {
            String paymentNo = generateUniquePaymentNo();
            payment.setPaymentNo(paymentNo);
            this.save(payment);
            return payment;
        }, "生成支付单号失败，请稍后重试");
        
        return payment;
    }
    
    /**
     * 生成唯一的支付单号
     * 格式：PAY + yyyyMMdd + 4位序号（从数据库中查询当天最大序号+1）
     */
    private String generateUniquePaymentNo() {
        String prefix = UniqueNoGenerator.buildFullPrefix("PAY");
        
        // 查询当天最大的支付单号
        LambdaQueryWrapper<OrderPayment> wrapper = new QueryWrapper<OrderPayment>().lambda();
        wrapper.likeRight(OrderPayment::getPaymentNo, prefix);
        wrapper.orderByDesc(OrderPayment::getPaymentNo);
        wrapper.last("LIMIT 1");
        
        OrderPayment lastPayment = this.getOne(wrapper);
        String lastNo = lastPayment != null ? lastPayment.getPaymentNo() : null;
        
        return UniqueNoGenerator.generateNo("PAY", lastNo);
    }

    /**
     * 处理支付
     * 处理订单支付的业务逻辑，包括：
     * 1. 验证订单状态
     * 2. 创建支付记录
     * 3. 模拟支付处理（实际项目中应调用第三方支付接口）
     * 4. 更新订单状态为已支付
     * 5. 更新菜品库存（支付成功后减少库存）
     * 6. 更新客户订单数和累计消费
     * 
     * @param orderId 订单ID
     * @param paymentMethod 支付方式
     * @throws RuntimeException 如果订单不存在、订单状态错误、支付失败等情况
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processPayment(Long orderId, String paymentMethod) {
        // 获取订单
        CustomerOrder order = orderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态错误，无法支付");
        }

        // 创建支付记录
        OrderPayment payment = createPayment(orderId, paymentMethod, order.getActualAmount());

        // 模拟支付处理（这里直接设置为支付成功，实际项目中应该调用第三方支付接口）
        try {
            // 模拟支付延迟
            Thread.sleep(500);
            
            // 模拟支付成功（实际项目中应该根据第三方支付接口返回结果判断）
            payment.setStatus(1); // 支付成功
            payment.setPayTime(LocalDateTime.now());
            this.updateById(payment);

            // 更新订单状态
            order.setStatus(1); // 已支付
            order.setPayTime(LocalDateTime.now());
            orderService.updateById(order);

            // 更新菜品库存（支付成功后减少库存）
            updateMenuItemStock(orderId);

            // 更新客户订单数和累计消费
            updateCustomerStatistics(order.getUserId(), order.getActualAmount());
        } catch (Exception e) {
            // 支付失败
            payment.setStatus(2); // 支付失败
            payment.setRemark("支付失败: " + e.getMessage());
            this.updateById(payment);
            throw new RuntimeException("支付失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<OrderPayment> getPaymentPage(Long current, Long size, Long orderId, Integer status) {
        Page<OrderPayment> page = new Page<>(current, size);
        LambdaQueryWrapper<OrderPayment> wrapper = new QueryWrapper<OrderPayment>().lambda();
        
        if (orderId != null) {
            wrapper.eq(OrderPayment::getOrderId, orderId);
        }
        if (status != null) {
            wrapper.eq(OrderPayment::getStatus, status);
        }
        
        wrapper.orderByDesc(OrderPayment::getCreateTime);
        Page<OrderPayment> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }

    /**
     * 更新菜品库存（支付成功后减少库存）
     * 使用数据库原子操作确保并发安全
     * 
     * 注意：由于在订单创建时已检查库存，支付时库存应该充足。
     * 如果更新失败，说明在支付过程中库存被其他订单消耗，此时应抛出异常回滚支付，
     * 确保数据一致性。
     */
    private void updateMenuItemStock(Long orderId) {
        // 获取订单详情列表
        LambdaQueryWrapper<OrderDetail> wrapper = new QueryWrapper<OrderDetail>().lambda();
        wrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper);

        // 遍历订单详情，更新每个菜品的库存
        for (OrderDetail detail : orderDetails) {
            Long itemId = detail.getItemId();
            Integer quantity = detail.getQuantity();

            if (itemId == null || quantity == null || quantity <= 0) {
                continue; // 跳过无效的订单详情
            }

            // 使用原子操作更新库存：stock = stock - quantity
            // WHERE 条件确保库存足够，防止库存变为负数
            LambdaUpdateWrapper<MenuItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MenuItem::getItemId, itemId)
                    .setSql("stock = stock - " + quantity)
                    .ge(MenuItem::getStock, quantity); // 确保当前库存 >= 订购数量

            int updated = menuItemMapper.update(null, updateWrapper);
            if (updated == 0) {
                // 如果更新失败（可能是库存不足），抛出异常回滚支付，确保数据一致性
                MenuItem menuItem = menuItemMapper.selectById(itemId);
                String itemName = menuItem != null ? menuItem.getItemName() : "未知菜品";
                int currentStock = menuItem != null && menuItem.getStock() != null ? menuItem.getStock() : 0;
                throw new RuntimeException("菜品库存不足，支付失败。菜品: " + itemName + "，当前库存: " + currentStock + "，需要数量: " + quantity);
            }
        }
    }

    /**
     * 更新客户订单数和累计消费（支付成功后调用）
     * 使用数据库原子操作确保并发安全
     * 
     * @param userId 用户ID
     * @param amount 订单实付金额
     */
    private void updateCustomerStatistics(Long userId, BigDecimal amount) {
        if (userId == null || amount == null) {
            return;
        }

        // 使用原子操作更新：total_orders = total_orders + 1, total_consumption = total_consumption + amount
        // 使用 IFNULL 代替 COALESCE（MySQL兼容性更好）
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getUserId, userId)
                .setSql("total_orders = IFNULL(total_orders, 0) + 1")
                .setSql("total_consumption = IFNULL(total_consumption, 0) + " + amount)
                .set(SysUser::getLastConsumeTime, LocalDateTime.now());

        userMapper.update(null, updateWrapper);
    }

    /**
     * 退款处理
     * 处理订单退款业务逻辑，包括：
     * 1. 检查支付记录状态（必须为支付成功）
     * 2. 检查订单状态（必须为已取消）
     * 3. 更新支付记录状态为已退款
     * 4. 更新订单状态为已退款
     * 5. 记录退款时间和原因
     * 
     * @param paymentId 支付记录ID
     * @param reason 退款原因
     * @throws RuntimeException 如果支付记录不存在、支付状态不正确、订单未取消等情况
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long paymentId, String reason) {
        // 获取支付记录
        OrderPayment payment = this.getById(paymentId);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }

        // 检查支付状态：只有支付成功(1)的才能退款
        // 如果已经是已退款(3)状态，不能再退款
        if (payment.getStatus() == 3) {
            throw new RuntimeException("该支付记录已退款，无需重复退款");
        }
        if (payment.getStatus() != 1) {
            throw new RuntimeException("只有支付成功的订单才能退款");
        }

        // 获取关联的订单
        CustomerOrder order = orderService.getById(payment.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查订单状态：只有已取消(4)的订单才能退款
        if (order.getStatus() != 4) {
            throw new RuntimeException("该订单正常操作，无法进行退款");
        }

        // 更新支付记录状态为已退款
        payment.setStatus(3); // 已退款
        payment.setRefundTime(LocalDateTime.now());
        if (reason != null && !reason.trim().isEmpty()) {
            String remarkText = (payment.getRemark() != null ? payment.getRemark() + "\n" : "") + "退款原因：" + reason;
            payment.setRemark(remarkText);
        }
        this.updateById(payment);

        // 更新订单状态为已退款
        order.setStatus(5); // 已退款
        if (reason != null && !reason.trim().isEmpty()) {
            String remarkText = (order.getRemark() != null ? order.getRemark() + "\n" : "") + "退款原因：" + reason;
            order.setRemark(remarkText);
        }
        orderService.updateById(order);
    }
}

