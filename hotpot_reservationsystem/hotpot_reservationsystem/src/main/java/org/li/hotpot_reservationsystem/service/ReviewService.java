package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.dto.ReviewDTO;
import org.li.hotpot_reservationsystem.entity.Review;

/**
 * 评论Service接口
 */
public interface ReviewService extends IService<Review> {
    /**
     * 分页查询评论列表（包含用户信息）
     */
    PageResult<ReviewDTO> getReviewPage(Long current, Long size, Integer status, Long userId, Long orderId, Integer rating);
}

