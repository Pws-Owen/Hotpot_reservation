package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.annotation.LogOperation;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.entity.SysRole;
import org.li.hotpot_reservationsystem.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理Controller
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Autowired
    private SysRoleService roleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping("/page")
    public Result<PageResult<SysRole>> getRolePage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {
        try {
            PageResult<SysRole> result = roleService.getRolePage(current, size, roleName, roleCode, status);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取角色列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有角色列表（不分页）
     */
    @GetMapping("/list")
    public Result<List<SysRole>> getAllRoles() {
        try {
            List<SysRole> roles = roleService.list();
            return Result.success(roles);
        } catch (Exception e) {
            return Result.error("获取角色列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public Result<SysRole> getRoleById(@PathVariable Long id) {
        try {
            SysRole role = roleService.getById(id);
            if (role == null) {
                return Result.error("角色不存在");
            }
            return Result.success(role);
        } catch (Exception e) {
            return Result.error("获取角色详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建角色
     */
    @PostMapping
    @LogOperation(operation = "新增角色")
    public Result<SysRole> createRole(@RequestBody SysRole role) {
        try {
            // 检查角色编码是否已存在
            SysRole existingRole = roleService.lambdaQuery()
                    .eq(SysRole::getRoleCode, role.getRoleCode())
                    .one();
            if (existingRole != null) {
                return Result.error("角色编码已存在");
            }

            roleService.save(role);
            return Result.success(role);
        } catch (Exception e) {
            return Result.error("创建角色失败: " + e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @LogOperation(operation = "编辑角色")
    public Result<SysRole> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        try {
            SysRole existingRole = roleService.getById(id);
            if (existingRole == null) {
                return Result.error("角色不存在");
            }

            // 检查角色编码是否与其他角色冲突
            if (role.getRoleCode() != null && !role.getRoleCode().equals(existingRole.getRoleCode())) {
                SysRole conflictRole = roleService.lambdaQuery()
                        .eq(SysRole::getRoleCode, role.getRoleCode())
                        .ne(SysRole::getRoleId, id)
                        .one();
                if (conflictRole != null) {
                    return Result.error("角色编码已存在");
                }
            }

            role.setRoleId(id);
            roleService.updateById(role);
            return Result.success(role);
        } catch (Exception e) {
            return Result.error("更新角色失败: " + e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @LogOperation(operation = "删除角色")
    public Result<Void> deleteRole(@PathVariable Long id) {
        try {
            SysRole role = roleService.getById(id);
            if (role == null) {
                return Result.error("角色不存在");
            }
            roleService.removeById(id);
            return Result.success((Void) null);
        } catch (Exception e) {
            return Result.error("删除角色失败: " + e.getMessage());
        }
    }

    /**
     * 分配权限
     */
    @PostMapping("/{id}/assign-permissions")
    @LogOperation(operation = "分配权限")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        try {
            SysRole role = roleService.getById(id);
            if (role == null) {
                return Result.error("角色不存在");
            }
            roleService.assignPermissions(id, permissionIds);
            return Result.success((Void) null);
        } catch (Exception e) {
            return Result.error("分配权限失败: " + e.getMessage());
        }
    }

    /**
     * 获取角色的权限ID列表
     */
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        try {
            List<Long> permissionIds = roleService.getPermissionIdsByRoleId(id);
            return Result.success(permissionIds);
        } catch (Exception e) {
            return Result.error("获取角色权限失败: " + e.getMessage());
        }
    }

    /**
     * 获取角色的用户ID列表
     */
    @GetMapping("/{id}/users")
    public Result<List<Long>> getRoleUsers(@PathVariable Long id) {
        try {
            List<Long> userIds = roleService.getUserIdsByRoleId(id);
            return Result.success(userIds);
        } catch (Exception e) {
            return Result.error("获取角色用户失败: " + e.getMessage());
        }
    }
}

