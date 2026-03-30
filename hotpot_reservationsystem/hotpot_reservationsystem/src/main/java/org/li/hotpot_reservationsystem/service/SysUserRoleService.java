package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.SysUserRole;

/**
 * 用户角色关联Service接口
 */
public interface SysUserRoleService extends IService<SysUserRole> {
    /**
     * 分页查询用户角色关联列表
     */
    PageResult<SysUserRole> getUserRolePage(Long current, Long size, Long userId, Long roleId);

    /**
     * 创建用户角色关联
     */
    void createUserRole(Long userId, Long roleId);

    /**
     * 更新用户角色关联
     */
    void updateUserRole(Long id, Long userId, Long roleId);

    /**
     * 删除用户角色关联
     */
    void deleteUserRole(Long id);
}

