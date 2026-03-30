package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.entity.SysPermission;

import java.util.List;

/**
 * 权限Service接口
 */
public interface SysPermissionService extends IService<SysPermission> {
    /**
     * 获取所有权限列表
     */
    List<SysPermission> getAllPermissions();
}

