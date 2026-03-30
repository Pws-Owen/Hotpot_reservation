package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.Announcement;
import org.li.hotpot_reservationsystem.mapper.AnnouncementMapper;
import org.li.hotpot_reservationsystem.service.AnnouncementService;
import org.springframework.stereotype.Service;

/**
 * 公告Service实现类
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public PageResult<Announcement> getAnnouncementPage(Long current, Long size, Integer status, Integer type, String title) {
        Page<Announcement> page = new Page<>(current, size);
        LambdaQueryWrapper<Announcement> wrapper = new QueryWrapper<Announcement>().lambda();
        
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        if (type != null) {
            wrapper.eq(Announcement::getType, type);
        }
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Announcement::getTitle, title);
        }
        
        // 按置顶、发布时间倒序排列
        wrapper.orderByDesc(Announcement::getIsTop);
        wrapper.orderByDesc(Announcement::getPublishTime);
        wrapper.orderByDesc(Announcement::getCreateTime);
        
        Page<Announcement> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }
}

