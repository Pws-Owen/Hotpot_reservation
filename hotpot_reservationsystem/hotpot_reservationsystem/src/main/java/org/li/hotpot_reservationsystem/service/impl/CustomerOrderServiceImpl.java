package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.dto.CreateOrderRequest;
import org.li.hotpot_reservationsystem.entity.CustomerOrder;
import org.li.hotpot_reservationsystem.entity.MenuItem;
import org.li.hotpot_reservationsystem.entity.OrderDetail;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.mapper.CustomerOrderMapper;
import org.li.hotpot_reservationsystem.mapper.MenuItemMapper;
import org.li.hotpot_reservationsystem.mapper.OrderDetailMapper;
import org.li.hotpot_reservationsystem.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.li.hotpot_reservationsystem.utils.RetryUtil;
import org.li.hotpot_reservationsystem.utils.UniqueNoGenerator;
import org.li.hotpot_reservationsystem.service.CustomerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单Service实现类
 * 处理订单相关的业务逻辑，包括订单创建、取消、查询等
 */
@Service
public class CustomerOrderServiceImpl extends ServiceImpl<CustomerOrderMapper, CustomerOrder> implements CustomerOrderService {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private MenuItemMapper menuItemMapper;

    @Autowired
    private org.li.hotpot_reservationsystem.mapper.RestaurantTableMapper restaurantTableMapper;

    @Autowired
    private org.li.hotpot_reservationsystem.mapper.ReservationMapper reservationMapper;

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 创建订单
     * 处理订单创建的业务逻辑，包括：
     * 1. 生成唯一订单号（带重试机制防止并发冲突）
     * 2. 验证菜品是否存在、是否上架、库存是否充足
     * 3. 计算订单总价
     * 4. 保存订单和订单详情
     * 
     * @param request 订单创建请求，包含订单详情、预约ID、餐桌ID等信息
     * @param userId 用户ID
     * @return 创建成功的订单对象
     * @throws RuntimeException 如果菜品不存在、已下架、库存不足等情况
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerOrder createOrder(CreateOrderRequest request, Long userId) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("订单详情不能为空");
        }

        // 创建订单（带重试机制，防止并发冲突）
        CustomerOrder order = new CustomerOrder();
        order.setUserId(userId);
        order.setReservationId(request.getReservationId());
        order.setTableId(request.getTableId());
        order.setOrderType(request.getOrderType() != null ? request.getOrderType() : 1); // 默认堂食
        order.setRemark(request.getRemark());
        order.setStatus(0); // 待支付
        
        // 生成唯一的订单号（带重试机制）
        String orderNo = RetryUtil.executeWithRetryForException(
            () -> generateUniqueOrderNo(),
            10,
            "生成订单号失败，请稍后重试"
        );
        order.setOrderNo(orderNo);

        // 计算订单总价，同时检查库存（第一次循环：查询并缓存MenuItem）
        Map<Long, MenuItem> menuItemMap = new HashMap<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CreateOrderRequest.OrderDetailItem item : request.getItems()) {
            MenuItem menuItem = menuItemMapper.selectById(item.getItemId());
            if (menuItem == null) {
                throw new RuntimeException("菜品不存在: " + item.getItemId());
            }
            if (menuItem.getStatus() != 1) {
                throw new RuntimeException("菜品已下架: " + menuItem.getItemName());
            }
            // 检查库存是否充足
            int currentStock = menuItem.getStock() != null ? menuItem.getStock() : 0;
            if (currentStock < item.getQuantity()) {
                throw new RuntimeException("菜品库存不足: " + menuItem.getItemName() + "，当前库存: " + currentStock + "，需要数量: " + item.getQuantity());
            }
            // 缓存MenuItem，避免重复查询
            menuItemMap.put(item.getItemId(), menuItem);
            
            BigDecimal price = menuItem.getPrice();
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());
            totalPrice = totalPrice.add(price.multiply(quantity));
        }

        order.setTotalPrice(totalPrice);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setActualAmount(totalPrice);

        // 保存订单（带重试机制，防止订单号重复）
        RetryUtil.executeWithRetry(() -> {
            try {
                this.save(order);
                return order;
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 如果订单号重复，重新生成并重试
                String newOrderNo = generateUniqueOrderNo();
                order.setOrderNo(newOrderNo);
                throw e; // 重新抛出，让重试机制处理
            }
        }, "生成订单号失败，请稍后重试");

        // 创建订单详情（第二次循环：从缓存Map中获取MenuItem）
        for (CreateOrderRequest.OrderDetailItem item : request.getItems()) {
            MenuItem menuItem = menuItemMap.get(item.getItemId());
            if (menuItem == null) {
                throw new RuntimeException("菜品信息不存在: " + item.getItemId());
            }
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(order.getOrderId());
            detail.setItemId(item.getItemId());
            detail.setItemName(menuItem.getItemName());
            detail.setItemImage(menuItem.getImageUrl());
            detail.setPrice(menuItem.getPrice());
            detail.setQuantity(item.getQuantity());
            detail.setSubtotal(menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderDetailMapper.insert(detail);
        }

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        CustomerOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 检查订单状态：只允许取消待支付(0)、已支付(1)、制作中(2)的订单
        // 不能取消已完成(3)、已取消(4)、已退款(5)的订单
        if (order.getStatus() == 3) {
            throw new RuntimeException("已完成的订单无法取消");
        }
        if (order.getStatus() == 4) {
            throw new RuntimeException("订单已取消，无需重复取消");
        }
        if (order.getStatus() == 5) {
            throw new RuntimeException("已退款的订单无法取消");
        }
        
        // 判断订单是否已支付（状态1-已支付或状态2-制作中）
        boolean isPaid = order.getStatus() == 1 || order.getStatus() == 2;
        
        // 更新订单状态为已取消
        order.setStatus(4); // 已取消
        if (reason != null) {
            order.setRemark((order.getRemark() != null ? order.getRemark() + "\n" : "") + "取消原因：" + reason);
        }
        this.updateById(order);
        
        // 如果订单已支付，需要恢复库存并减少客户统计
        if (isPaid) {
            // 恢复菜品库存
            restoreMenuItemStock(orderId);
            
            // 减少客户订单数和累计消费
            reduceCustomerStatistics(order.getUserId(), order.getActualAmount());
        }
    }

    /**
     * 恢复菜品库存（取消已支付的订单时调用）
     * 将订单中的菜品数量加回到库存中
     * 
     * @param orderId 订单ID
     */
    private void restoreMenuItemStock(Long orderId) {
        // 获取订单详情列表
        LambdaQueryWrapper<OrderDetail> wrapper = new QueryWrapper<OrderDetail>().lambda();
        wrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(wrapper);

        // 遍历订单详情，恢复每个菜品的库存
        for (OrderDetail detail : orderDetails) {
            Long itemId = detail.getItemId();
            Integer quantity = detail.getQuantity();

            if (itemId == null || quantity == null || quantity <= 0) {
                continue; // 跳过无效的订单详情
            }

            // 使用原子操作恢复库存：stock = stock + quantity
            LambdaUpdateWrapper<MenuItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MenuItem::getItemId, itemId)
                    .setSql("stock = IFNULL(stock, 0) + " + quantity);

            menuItemMapper.update(null, updateWrapper);
        }
    }

    /**
     * 减少客户订单数和累计消费（取消已支付的订单时调用）
     * 使用数据库原子操作确保并发安全
     * 
     * @param userId 用户ID
     * @param amount 订单实付金额
     */
    private void reduceCustomerStatistics(Long userId, BigDecimal amount) {
        if (userId == null || amount == null) {
            return;
        }

        // 使用原子操作更新：total_orders = total_orders - 1, total_consumption = total_consumption - amount
        // 使用 IFNULL 确保空值处理，使用 GREATEST 确保值不会小于0
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getUserId, userId)
                .setSql("total_orders = GREATEST(IFNULL(total_orders, 0) - 1, 0)")
                .setSql("total_consumption = GREATEST(IFNULL(total_consumption, 0) - " + amount + ", 0)");

        userMapper.update(null, updateWrapper);
    }

    @Autowired
    @org.springframework.context.annotation.Lazy
    private org.li.hotpot_reservationsystem.service.OrderPaymentService paymentService;

    /**
     * 支付订单
     * 委托给支付服务处理支付逻辑
     * 
     * @param orderId 订单ID
     * @param paymentMethod 支付方式（如：WECHAT, ALIPAY等）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, String paymentMethod) {
        // 使用支付服务处理支付
        paymentService.processPayment(orderId, paymentMethod);
    }

    /**
     * 更新订单状态
     * 根据不同的目标状态进行相应的业务处理：
     * - 完成订单（状态3）：需要订单当前状态为制作中(2)，并设置完成时间
     * - 其他状态：直接更新
     * 
     * @param orderId 订单ID
     * @param status 目标状态（0-待支付，1-已支付，2-制作中，3-已完成，4-已取消，5-已退款）
     * @throws RuntimeException 如果订单不存在或状态转换不合法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long orderId, Integer status) {
        CustomerOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 状态验证
        if (status == 3) { // 已完成
            if (order.getStatus() != 2) {
                throw new RuntimeException("订单状态错误，只有制作中的订单才能完成");
            }
            order.setFinishTime(LocalDateTime.now());
        } else if (status == 2) { // 制作中
            if (order.getStatus() != 1) {
                throw new RuntimeException("订单状态错误，只有已支付的订单才能开始制作");
            }
        }
        
        order.setStatus(status);
        this.updateById(order);
    }

    @Override
    public List<OrderDetail> getOrderDetails(Long orderId) {
        LambdaQueryWrapper<OrderDetail> wrapper = new QueryWrapper<OrderDetail>().lambda();
        wrapper.eq(OrderDetail::getOrderId, orderId);
        return orderDetailMapper.selectList(wrapper);
    }

    @Override
    public PageResult<? extends CustomerOrder> getOrderPage(Long current, Long size, Integer status, Long userId, LocalDate date) {
        Page<CustomerOrder> page = new Page<>(current, size);
        LambdaQueryWrapper<CustomerOrder> wrapper = new QueryWrapper<CustomerOrder>().lambda();
        
        if (status != null) {
            wrapper.eq(CustomerOrder::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(CustomerOrder::getUserId, userId);
        }
        if (date != null) {
            wrapper.ge(CustomerOrder::getCreateTime, date.atStartOfDay())
                   .lt(CustomerOrder::getCreateTime, date.plusDays(1).atStartOfDay());
        }
        
        wrapper.orderByDesc(CustomerOrder::getCreateTime);
        Page<CustomerOrder> result = this.page(page, wrapper);
        
        // 填充桌子号和预约号
        List<org.li.hotpot_reservationsystem.dto.OrderWithTableReservationDTO> dtoList = new ArrayList<>();
        for (CustomerOrder order : result.getRecords()) {
            org.li.hotpot_reservationsystem.dto.OrderWithTableReservationDTO dto = new org.li.hotpot_reservationsystem.dto.OrderWithTableReservationDTO();
            org.springframework.beans.BeanUtils.copyProperties(order, dto);
            
            // 查询桌子号
            if (order.getTableId() != null) {
                org.li.hotpot_reservationsystem.entity.RestaurantTable table = restaurantTableMapper.selectById(order.getTableId());
                if (table != null) {
                    dto.setTableNumber(table.getTableNumber());
                }
            }
            
            // 查询预约号
            if (order.getReservationId() != null) {
                org.li.hotpot_reservationsystem.entity.Reservation reservation = reservationMapper.selectById(order.getReservationId());
                if (reservation != null) {
                    dto.setReservationNo(reservation.getReservationNo());
                }
            }
            
            // 查询用户真实姓名
            if (order.getUserId() != null) {
                org.li.hotpot_reservationsystem.entity.SysUser user = userMapper.selectById(order.getUserId());
                if (user != null) {
                    dto.setRealName(user.getRealName());
                }
            }
            
            dtoList.add(dto);
        }
        
        // 创建新的分页结果
        Page<org.li.hotpot_reservationsystem.dto.OrderWithTableReservationDTO> dtoPage = new Page<>(current, size, result.getTotal());
        dtoPage.setRecords(dtoList);
        
        return PageResult.of(dtoPage);
    }
    
    /**
     * 生成唯一的订单号
     * 格式：ORD + yyyyMMdd + 4位序号（从数据库中查询当天最大序号+1）
     */
    private String generateUniqueOrderNo() {
        String prefix = UniqueNoGenerator.buildFullPrefix("ORD");
        
        // 查询当天最大的订单号
        LambdaQueryWrapper<CustomerOrder> wrapper = new QueryWrapper<CustomerOrder>().lambda();
        wrapper.likeRight(CustomerOrder::getOrderNo, prefix);
        wrapper.orderByDesc(CustomerOrder::getOrderNo);
        wrapper.last("LIMIT 1");
        
        CustomerOrder lastOrder = this.getOne(wrapper);
        String lastNo = lastOrder != null ? lastOrder.getOrderNo() : null;
        
        return UniqueNoGenerator.generateNo("ORD", lastNo);
    }
}

