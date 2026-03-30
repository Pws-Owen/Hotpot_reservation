package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.SysRole;
import org.li.hotpot_reservationsystem.entity.SysRolePermission;
import org.li.hotpot_reservationsystem.entity.SysUserRole;
import org.li.hotpot_reservationsystem.mapper.SysRoleMapper;
import org.li.hotpot_reservationsystem.mapper.SysRolePermissionMapper;
import org.li.hotpot_reservationsystem.mapper.SysUserRoleMapper;
import org.li.hotpot_reservationsystem.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色Service实现类
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Override
    public PageResult<SysRole> getRolePage(Long current, Long size, String roleName, String roleCode, Integer status) {
        Page<SysRole> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRole> wrapper = new QueryWrapper<SysRole>().lambda();
        
        if (roleName != null && !roleName.trim().isEmpty()) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        if (roleCode != null && !roleCode.trim().isEmpty()) {
            wrapper.like(SysRole::getRoleCode, roleCode);
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        
        wrapper.orderByDesc(SysRole::getCreateTime);
        Page<SysRole> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限
        LambdaQueryWrapper<SysRolePermission> deleteWrapper = new QueryWrapper<SysRolePermission>().lambda();
        deleteWrapper.eq(SysRolePermission::getRoleId, roleId);
        rolePermissionMapper.delete(deleteWrapper);
        
        // 添加新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissionMapper.insert(rolePermission);
            }
        }
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new QueryWrapper<SysRolePermission>().lambda();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(wrapper);
        return rolePermissions.stream()
                .map(SysRolePermission::getPermissionId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getUserIdsByRoleId(Long roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new QueryWrapper<SysUserRole>().lambda();
        wrapper.eq(SysUserRole::getRoleId, roleId);
        List<SysUserRole> userRoles = userRoleMapper.selectList(wrapper);
        return userRoles.stream()
                .map(SysUserRole::getUserId)
                .collect(Collectors.toList());
    }
}

