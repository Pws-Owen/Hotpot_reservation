package org.li.hotpot_reservationsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.li.hotpot_reservationsystem.entity.Review;

/**
 * 评论Mapper接口
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}

