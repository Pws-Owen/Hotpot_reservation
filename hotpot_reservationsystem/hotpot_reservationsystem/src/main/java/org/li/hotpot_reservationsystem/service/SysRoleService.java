package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.SysRole;

import java.util.List;

/**
 * 角色Service接口
 */
public interface SysRoleService extends IService<SysRole> {
    /**
     * 分页查询角色列表
     */
    PageResult<SysRole> getRolePage(Long current, Long size, String roleName, String roleCode, Integer status);

    /**
     * 分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);

    /**
     * 获取角色的用户ID列表
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> getUserIdsByRoleId(Long roleId);
}

