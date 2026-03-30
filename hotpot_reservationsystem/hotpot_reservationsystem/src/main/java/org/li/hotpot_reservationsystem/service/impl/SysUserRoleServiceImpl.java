package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.SysUserRole;
import org.li.hotpot_reservationsystem.mapper.SysUserRoleMapper;
import org.li.hotpot_reservationsystem.service.SysUserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户角色关联Service实现类
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Override
    public PageResult<SysUserRole> getUserRolePage(Long current, Long size, Long userId, Long roleId) {
        // 直接查询sys_user_role表，不进行关联查询
        Page<SysUserRole> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUserRole> wrapper = new QueryWrapper<SysUserRole>().lambda();
        
        if (userId != null) {
            wrapper.eq(SysUserRole::getUserId, userId);
        }
        if (roleId != null) {
            wrapper.eq(SysUserRole::getRoleId, roleId);
        }
        
        wrapper.orderByDesc(SysUserRole::getCreateTime);
        Page<SysUserRole> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUserRole(Long userId, Long roleId) {
        // 检查是否已存在
        LambdaQueryWrapper<SysUserRole> wrapper = new QueryWrapper<SysUserRole>().lambda();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getRoleId, roleId);
        SysUserRole existing = this.getOne(wrapper);
        if (existing != null) {
            throw new RuntimeException("该用户已拥有此角色");
        }
        
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        this.save(userRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRole(Long id, Long userId, Long roleId) {
        // 检查关联是否存在
        SysUserRole userRole = this.getById(id);
        if (userRole == null) {
            throw new RuntimeException("用户角色关联不存在");
        }
        
        // 检查新的关联是否已存在（排除当前记录）
        LambdaQueryWrapper<SysUserRole> wrapper = new QueryWrapper<SysUserRole>().lambda();
        wrapper.eq(SysUserRole::getUserId, userId)
               .eq(SysUserRole::getRoleId, roleId)
               .ne(SysUserRole::getId, id);
        SysUserRole existing = this.getOne(wrapper);
        if (existing != null) {
            throw new RuntimeException("该用户已拥有此角色");
        }
        
        // 更新关联
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        this.updateById(userRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserRole(Long id) {
        this.removeById(id);
    }
}

