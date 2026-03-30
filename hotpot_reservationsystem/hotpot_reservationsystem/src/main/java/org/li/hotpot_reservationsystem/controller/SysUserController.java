package org.li.hotpot_reservationsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.li.hotpot_reservationsystem.annotation.LogOperation;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.dto.CreateUserRequest;
import org.li.hotpot_reservationsystem.dto.UpdateUserRequest;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.entity.SysUserRole;
import org.li.hotpot_reservationsystem.mapper.SysUserRoleMapper;
import org.li.hotpot_reservationsystem.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户管理Controller
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表（所有用户）
     */
    @GetMapping("/page")
    public Result<PageResult<SysUser>> getUserPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer userType) {
        try {
            PageResult<SysUser> result = userService.getUserPage(current, size, username, realName, status, userType);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        try {
            SysUser user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建系统用户
     */
    @PostMapping
    @LogOperation(operation = "新增用户")
    public Result<SysUser> createUser(@RequestBody CreateUserRequest request) {
        try {
            // 检查用户名是否已存在
            SysUser existingUser = userService.getByUsername(request.getUsername());
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            // 创建用户
            SysUser user = new SysUser();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
            user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
            user.setUserType(request.getUserType() != null ? request.getUserType() : 1); // 默认为系统用户

            userService.save(user);

            // 只有系统用户才分配角色
            if (user.getUserType() == 1 && request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                assignRolesInternal(user.getUserId(), request.getRoleIds());
            }

            return Result.success(user);
        } catch (Exception e) {
            return Result.error("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @LogOperation(operation = "编辑用户")
    public Result<SysUser> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        try {
            SysUser user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 更新用户信息
            if (request.getRealName() != null) {
                user.setRealName(request.getRealName());
            }
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
            }
            if (request.getEmail() != null) {
                user.setEmail(request.getEmail());
            }
            if (request.getAvatar() != null) {
                user.setAvatar(request.getAvatar());
            }
            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
            }

            userService.updateById(user);

            // 只有系统用户才更新角色
            if (user.getUserType() == 1 && request.getRoleIds() != null) {
                assignRolesInternal(id, request.getRoleIds());
            }

            return Result.success(user);
        } catch (Exception e) {
            return Result.error("更新用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @LogOperation(operation = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            SysUser user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            userService.removeById(id);
            return Result.success((Void) null);
        } catch (Exception e) {
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    @LogOperation(operation = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        try {
            SysUser user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userService.updateById(user);

            return Result.success((Void) null);
        } catch (Exception e) {
            return Result.error("重置密码失败: " + e.getMessage());
        }
    }

    /**
     * 分配角色
     */
    @PostMapping("/{id}/assign-roles")
    @LogOperation(operation = "分配角色")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        try {
            assignRolesInternal(id, roleIds);
            return Result.success((Void) null);
        } catch (Exception e) {
            return Result.error("分配角色失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的角色ID列表
     */
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        try {
            List<Long> roleIds = userService.getRoleIdsByUserId(id);
            return Result.success(roleIds);
        } catch (Exception e) {
            return Result.error("获取用户角色失败: " + e.getMessage());
        }
    }

    /**
     * 分配角色的内部方法
     */
    private void assignRolesInternal(Long userId, List<Long> roleIds) {
        // 删除原有角色
        LambdaQueryWrapper<SysUserRole> deleteWrapper = new QueryWrapper<SysUserRole>().lambda();
        deleteWrapper.eq(SysUserRole::getUserId, userId);
        userRoleMapper.delete(deleteWrapper);

        // 添加新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }

    /**
     * 重置密码请求
     */
    public static class ResetPasswordRequest {
        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}

