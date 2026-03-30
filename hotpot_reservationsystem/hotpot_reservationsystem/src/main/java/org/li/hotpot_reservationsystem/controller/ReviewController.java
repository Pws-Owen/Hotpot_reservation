package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.annotation.LogOperation;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.dto.ReviewDTO;
import org.li.hotpot_reservationsystem.entity.Review;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.mapper.CustomerOrderMapper;
import org.li.hotpot_reservationsystem.mapper.SysUserMapper;
import org.li.hotpot_reservationsystem.service.ReviewService;
import org.springframework.beans.BeanUtils;
import org.li.hotpot_reservationsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 评论Controller
 */
@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private CustomerOrderMapper orderMapper;

    @Autowired
    private org.li.hotpot_reservationsystem.service.SysUserService userService;

    /**
     * 分页查询评论列表
     */
    @GetMapping("/page")
    public Result<PageResult<ReviewDTO>> getReviewPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer rating) {
        // 如果提供了订单号，先通过订单号查询订单ID
        if (orderNo != null && !orderNo.trim().isEmpty() && orderId == null) {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.li.hotpot_reservationsystem.entity.CustomerOrder> orderWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<org.li.hotpot_reservationsystem.entity.CustomerOrder>().lambda();
            orderWrapper.eq(org.li.hotpot_reservationsystem.entity.CustomerOrder::getOrderNo, orderNo.trim());
            org.li.hotpot_reservationsystem.entity.CustomerOrder order = orderMapper.selectOne(orderWrapper);
            if (order != null) {
                orderId = order.getOrderId();
            } else {
                // 订单不存在，返回空列表
                PageResult<ReviewDTO> emptyResult = new PageResult<>();
                emptyResult.setRecords(java.util.Collections.emptyList());
                emptyResult.setTotal(0L);
                emptyResult.setSize(size);
                emptyResult.setCurrent(current);
                emptyResult.setPages(0L);
                return Result.success(emptyResult);
            }
        }
        
        // 权限检查逻辑：
        // 1. 如果查询公开评价（status=1）且未指定userId，所有用户（包括未登录）都可以查看所有公开评价
        // 2. 如果指定了userId，需要检查权限（普通用户只能查看自己的评价，管理员可以查看所有）
        // 3. 如果查询非公开评价或未登录，需要管理员权限
        
        boolean isQueryingPublicReviews = (status != null && status == 1 && userId == null);
        
        if (isQueryingPublicReviews) {
            // 查询公开评价且未指定用户，允许所有用户查看（包括未登录）
            // userId 保持为 null，查询所有用户的公开评价
        } else {
            // 查询特定用户的评价或非公开评价，需要权限检查
            if (SecurityUtil.isAuthenticated()) {
                try {
                    Long currentUserId = SecurityUtil.getCurrentUserId();
                    
                    // 获取当前用户的角色编码列表
                    java.util.List<String> roleCodes = userService.getRoleCodesByUserId(currentUserId);
                    
                    // 判断是否是管理员（ADMIN）或前台服务员（RECEPTIONIST）
                    boolean isAdmin = roleCodes != null && (roleCodes.contains("ADMIN") || roleCodes.contains("RECEPTIONIST"));
                    
                    if (!isAdmin) {
                        // 普通用户只能查看自己的评论，强制设置为当前用户ID
                        if (userId == null) {
                            userId = currentUserId;
                        } else if (!userId.equals(currentUserId)) {
                            // 普通用户不能查看其他用户的非公开评价
                            throw new RuntimeException("无权查看其他用户的评价");
                        }
                    }
                    // 如果是管理员或前台，userId 可以为 null（查看所有评论）或指定 userId（查看特定用户的评论）
                } catch (Exception e) {
                    // 如果无法获取用户ID，返回错误
                    throw new RuntimeException("无法获取用户信息", e);
                }
            } else {
                // 未登录用户，只能查询公开评价且未指定userId的情况
                // 其他情况需要登录
                throw new RuntimeException("用户未登录");
            }
        }
        
        PageResult<ReviewDTO> result = reviewService.getReviewPage(current, size, status, userId, orderId, rating);
        return Result.success(result);
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/{id}")
    public Result<ReviewDTO> getReviewById(@PathVariable Long id) {
        try {
            Review review = reviewService.getById(id);
            if (review == null) {
                return Result.error("评论不存在");
            }
            
            // 转换为DTO
            ReviewDTO dto = new ReviewDTO();
            BeanUtils.copyProperties(review, dto);
            
            // 查询用户信息
            if (review.getUserId() != null) {
                SysUser user = userMapper.selectById(review.getUserId());
                if (user != null) {
                    dto.setUserName(user.getUsername());
                    dto.setRealName(user.getRealName());
                }
            }
            
            // 查询订单信息
            if (review.getOrderId() != null) {
                org.li.hotpot_reservationsystem.entity.CustomerOrder order = orderMapper.selectById(review.getOrderId());
                if (order != null) {
                    dto.setOrderNo(order.getOrderNo());
                }
            }
            
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建评论
     */
    @PostMapping
    public Result<Review> createReview(@RequestBody Review review) {
        Long userId = SecurityUtil.getCurrentUserId();
        review.setUserId(userId);
        
        // 设置默认状态为显示
        if (review.getStatus() == null) {
            review.setStatus(1);
        }
        
        reviewService.save(review);
        return Result.success(review);
    }

    /**
     * 更新评论
     */
    @PutMapping("/{id}")
    public Result<Review> updateReview(@PathVariable Long id, @RequestBody Review review) {
        Review existing = reviewService.getById(id);
        if (existing == null) {
            throw new RuntimeException("评论不存在");
        }
        
        // 检查权限：只能修改自己的评论
        if (SecurityUtil.isAuthenticated()) {
            Authentication authentication = SecurityUtil.getAuthentication();
            Long currentUserId = SecurityUtil.getCurrentUserId();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RECEPTIONIST"));
            if (!isAdmin && !existing.getUserId().equals(currentUserId)) {
                throw new RuntimeException("无权修改此评论");
            }
        }
        
        review.setReviewId(id);
        reviewService.updateById(review);
        return Result.success(review);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        Review review = reviewService.getById(id);
        if (review == null) {
            throw new RuntimeException("评论不存在");
        }
        
        // 检查权限：只能删除自己的评论
        if (SecurityUtil.isAuthenticated()) {
            Authentication authentication = SecurityUtil.getAuthentication();
            Long currentUserId = SecurityUtil.getCurrentUserId();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RECEPTIONIST"));
            if (!isAdmin && !review.getUserId().equals(currentUserId)) {
                throw new RuntimeException("无权删除此评论");
            }
        }
        
        reviewService.removeById(id);
        return Result.success();
    }

    /**
     * 回复评论
     */
    @PostMapping("/{id}/reply")
    @LogOperation(operation = "回复评论")
    public Result<Review> replyReview(@PathVariable Long id, @RequestBody ReplyRequest request) {
        Review review = reviewService.getById(id);
        if (review == null) {
            throw new RuntimeException("评论不存在");
        }
        
        review.setReply(request.getReply());
        review.setReplyTime(java.time.LocalDateTime.now());
        reviewService.updateById(review);
        return Result.success(review);
    }

    /**
     * 回复请求DTO
     */
    public static class ReplyRequest {
        private String reply;

        public String getReply() {
            return reply;
        }

        public void setReply(String reply) {
            this.reply = reply;
        }
    }
}

