package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.dto.ReviewDTO;
import org.li.hotpot_reservationsystem.entity.CustomerOrder;
import org.li.hotpot_reservationsystem.entity.Review;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.mapper.CustomerOrderMapper;
import org.li.hotpot_reservationsystem.mapper.ReviewMapper;
import org.li.hotpot_reservationsystem.mapper.SysUserMapper;
import org.li.hotpot_reservationsystem.service.ReviewService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论Service实现类
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private CustomerOrderMapper orderMapper;

    @Override
    public PageResult<ReviewDTO> getReviewPage(Long current, Long size, Integer status, Long userId, Long orderId, Integer rating) {
        Page<Review> page = new Page<>(current, size);
        LambdaQueryWrapper<Review> wrapper = new QueryWrapper<Review>().lambda();
        
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(Review::getUserId, userId);
        }
        if (orderId != null) {
            wrapper.eq(Review::getOrderId, orderId);
        }
        if (rating != null) {
            wrapper.eq(Review::getRating, rating);
        }
        
        wrapper.orderByDesc(Review::getCreateTime);
        Page<Review> result = this.page(page, wrapper);
        
        // 转换为DTO并关联用户信息
        List<ReviewDTO> dtoList = result.getRecords().stream().map(review -> {
            ReviewDTO dto = new ReviewDTO();
            BeanUtils.copyProperties(review, dto);
            return dto;
        }).collect(Collectors.toList());
        
        // 关联用户信息和订单信息
        if (!dtoList.isEmpty()) {
            // 获取所有用户ID
            List<Long> userIds = dtoList.stream()
                    .map(ReviewDTO::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 获取所有订单ID
            List<Long> orderIds = dtoList.stream()
                    .map(ReviewDTO::getOrderId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 批量查询用户信息
            Map<Long, SysUser> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<SysUser> users = userMapper.selectBatchIds(userIds);
                userMap = users.stream()
                        .collect(Collectors.toMap(SysUser::getUserId, user -> user));
            }
            
            // 批量查询订单信息
            Map<Long, CustomerOrder> orderMap = new HashMap<>();
            if (!orderIds.isEmpty()) {
                List<CustomerOrder> orders = orderMapper.selectBatchIds(orderIds);
                orderMap = orders.stream()
                        .collect(Collectors.toMap(CustomerOrder::getOrderId, order -> order));
            }
            
            // 设置用户信息和订单信息
            final Map<Long, SysUser> finalUserMap = userMap;
            final Map<Long, CustomerOrder> finalOrderMap = orderMap;
            dtoList.forEach(dto -> {
                SysUser user = finalUserMap.get(dto.getUserId());
                if (user != null) {
                    dto.setUserName(user.getUsername());
                    dto.setRealName(user.getRealName());
                }
                
                if (dto.getOrderId() != null) {
                    CustomerOrder order = finalOrderMap.get(dto.getOrderId());
                    if (order != null) {
                        dto.setOrderNo(order.getOrderNo());
                    }
                }
            });
        }
        
        // 创建新的PageResult
        PageResult<ReviewDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtoList);
        pageResult.setTotal(result.getTotal());
        pageResult.setSize(result.getSize());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setPages(result.getPages());
        
        return pageResult;
    }
}

