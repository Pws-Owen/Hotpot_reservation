package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.entity.SysPermission;
import org.li.hotpot_reservationsystem.mapper.SysPermissionMapper;
import org.li.hotpot_reservationsystem.service.SysPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限Service实现类
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Override
    public List<SysPermission> getAllPermissions() {
        LambdaQueryWrapper<SysPermission> wrapper = new QueryWrapper<SysPermission>().lambda();
        wrapper.eq(SysPermission::getStatus, 1); // 只查询启用的权限
        wrapper.orderByAsc(SysPermission::getSortOrder);
        return this.list(wrapper);
    }
}

