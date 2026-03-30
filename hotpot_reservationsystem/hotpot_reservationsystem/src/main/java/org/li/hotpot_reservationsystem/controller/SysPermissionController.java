package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.entity.SysPermission;
import org.li.hotpot_reservationsystem.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理Controller
 */
@RestController
@RequestMapping("/system/permission")
public class SysPermissionController {

    @Autowired
    private SysPermissionService permissionService;

    /**
     * 获取所有权限列表
     */
    @GetMapping("/list")
    public Result<List<SysPermission>> getAllPermissions() {
        try {
            List<SysPermission> permissions = permissionService.getAllPermissions();
            return Result.success(permissions);
        } catch (Exception e) {
            return Result.error("获取权限列表失败: " + e.getMessage());
        }
    }
}

