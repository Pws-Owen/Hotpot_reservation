package org.li.hotpot_reservationsystem.controller;

import jakarta.validation.Valid;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.dto.ChangePasswordRequest;
import org.li.hotpot_reservationsystem.dto.LoginRequest;
import org.li.hotpot_reservationsystem.dto.LoginResponse;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.service.SysUserService;
import org.li.hotpot_reservationsystem.utils.JwtUtil;
import org.li.hotpot_reservationsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证Controller
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = userService.login(request.getUsername(), request.getPassword());
            SysUser user = userService.getByUsername(request.getUsername());
            
            // 获取用户角色编码列表
            List<String> roleCodes = userService.getRoleCodesByUserId(user.getUserId());
            
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setRealName(user.getRealName());
            response.setPhone(user.getPhone());
            response.setEmail(user.getEmail());
            response.setAvatar(user.getAvatar());
            response.setRoles(roleCodes);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<SysUser> register(@Valid @RequestBody LoginRequest request) {
        try {
            SysUser user = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getUsername(), // 这里简化处理，实际应该单独传phone
                request.getUsername()
            );
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     * 需要用户已登录，从JWT Token中获取用户ID
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        
        // 调用Service修改密码
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        
        // 返回成功结果，Void类型不需要数据
        Result<Void> result = Result.success((Void) null);
        result.setMessage("密码修改成功");
        return result;
    }
}

