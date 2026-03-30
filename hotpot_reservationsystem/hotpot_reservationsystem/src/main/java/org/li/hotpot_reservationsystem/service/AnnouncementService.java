package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.Announcement;

/**
 * 公告Service接口
 */
public interface AnnouncementService extends IService<Announcement> {
    /**
     * 分页查询公告列表
     */
    PageResult<Announcement> getAnnouncementPage(Long current, Long size, Integer status, Integer type, String title);
}

