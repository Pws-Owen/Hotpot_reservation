package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.annotation.LogOperation;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.entity.SysUserRole;
import org.li.hotpot_reservationsystem.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户角色关联Controller
 */
@RestController
@RequestMapping("/system/user-role")
public class SysUserRoleController {

    @Autowired
    private SysUserRoleService userRoleService;

    /**
     * 分页查询用户角色关联列表
     */
    @GetMapping("/page")
    public Result<PageResult<SysUserRole>> getUserRolePage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roleId) {
        try {
            PageResult<SysUserRole> result = userRoleService.getUserRolePage(current, size, userId, roleId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取用户角色列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建用户角色关联
     */
    @PostMapping
    @LogOperation(operation = "新增用户角色关联")
    public Result<Void> createUserRole(@RequestBody CreateUserRoleRequest request) {
        try {
            userRoleService.createUserRole(request.getUserId(), request.getRoleId());
            return Result.success((Void) null);
        } catch (Exception e) {
            return Result.error("创建用户角色关联失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户角色关联
     */
    @PutMapping("/{id}")
    @LogOperation(operation = "更新用户角色关联")
    public Result<Void> updateUserRole(@PathVariable Long id, @RequestBody UpdateUserRoleRequest request) {
        try {
            userRoleService.updateUserRole(id, request.getUserId(), request.getRoleId());
            return Result.success((Void) null);
        } catch (Exception e) {
            return Result.error("更新用户角色关联失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户角色关联
     */
    @DeleteMapping("/{id}")
    @LogOperation(operation = "删除用户角色关联")
    public Result<Void> deleteUserRole(@PathVariable Long id) {
        try {
            userRoleService.deleteUserRole(id);
            return Result.success((Void) null);
        } catch (Exception e) {
            return Result.error("删除用户角色关联失败: " + e.getMessage());
        }
    }

    /**
     * 创建用户角色关联请求
     */
    public static class CreateUserRoleRequest {
        private Long userId;
        private Long roleId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }
    }

    /**
     * 更新用户角色关联请求
     */
    public static class UpdateUserRoleRequest {
        private Long userId;
        private Long roleId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }
    }
}

