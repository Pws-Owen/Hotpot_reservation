package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.annotation.LogOperation;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.entity.Announcement;
import org.li.hotpot_reservationsystem.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告Controller
 */
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取所有公告（用于用户端，只返回已发布的）
     */
    @GetMapping
    public Result<List<Announcement>> getAllAnnouncements(
            @RequestParam(required = false) Integer status) {
        try {
            // 如果未指定status，默认只返回已发布的公告（status=1）
            if (status == null) {
                status = 1;
            }
            List<Announcement> announcements = announcementService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Announcement>()
                            .eq(Announcement::getStatus, status)
                            .orderByDesc(Announcement::getIsTop)
                            .orderByDesc(Announcement::getPublishTime)
            );
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询公告列表
     */
    @GetMapping("/page")
    public Result<PageResult<Announcement>> getAnnouncementPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String title) {
        try {
            PageResult<Announcement> result = announcementService.getAnnouncementPage(current, size, status, type, title);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Long id) {
        try {
            Announcement announcement = announcementService.getById(id);
            if (announcement == null) {
                return Result.error("公告不存在");
            }
            return Result.success(announcement);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建公告
     */
    @PostMapping
    @LogOperation(operation = "新增公告")
    public Result<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        try {
            // 如果未设置发布时间，使用当前时间
            if (announcement.getPublishTime() == null) {
                announcement.setPublishTime(java.time.LocalDateTime.now());
            }
            // 默认状态为启用
            if (announcement.getStatus() == null) {
                announcement.setStatus(1);
            }
            boolean success = announcementService.save(announcement);
            if (success) {
                return Result.success(announcement);
            } else {
                return Result.error("创建公告失败");
            }
        } catch (Exception e) {
            return Result.error("创建公告失败: " + e.getMessage());
        }
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    @LogOperation(operation = "编辑公告")
    public Result<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        try {
            announcement.setAnnouncementId(id);
            boolean success = announcementService.updateById(announcement);
            if (success) {
                return Result.success(announcement);
            } else {
                return Result.error("更新公告失败");
            }
        } catch (Exception e) {
            return Result.error("更新公告失败: " + e.getMessage());
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @LogOperation(operation = "删除公告")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        try {
            boolean success = announcementService.removeById(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除公告失败");
            }
        } catch (Exception e) {
            return Result.error("删除公告失败: " + e.getMessage());
        }
    }
}

