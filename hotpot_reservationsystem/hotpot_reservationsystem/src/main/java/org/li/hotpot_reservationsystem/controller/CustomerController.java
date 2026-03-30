package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.annotation.LogOperation;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户管理Controller
 * 注意：客户实际上就是普通用户（USER角色），这里提供客户管理的专用接口
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private SysUserService userService;

    /**
     * 分页查询客户列表
     */
    @GetMapping("/page")
    public Result<PageResult<SysUser>> getCustomerPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String tag) {
        // 查询普通用户角色（USER）的用户
        PageResult<SysUser> result = userService.getCustomerPage(current, size, name, phone);
        return Result.success(result);
    }

    /**
     * 创建客户（新增会员）
     */
    @PostMapping
    @LogOperation(operation = "新增会员")
    public Result<SysUser> createCustomer(@RequestBody CreateCustomerRequest request) {
        SysUser user = userService.register(
            request.getUsername(),
            request.getPassword(),
            request.getPhone(),
            request.getRealName()
        );
        // 如果提供了邮箱或VIP等级，统一更新
        boolean needUpdate = false;
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail().trim());
            needUpdate = true;
        }
        if (request.getVipLevel() != null) {
            user.setVipLevel(request.getVipLevel());
            needUpdate = true;
        }
        if (needUpdate) {
            userService.updateById(user);
        }
        return Result.success(user);
    }

    /**
     * 获取客户详情
     */
    @GetMapping("/{id}")
    public Result<SysUser> getCustomerById(@PathVariable Long id) {
        try {
            SysUser user = userService.getById(id);
            if (user == null) {
                return Result.error("客户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新客户信息
     */
    @PutMapping("/{id}")
    @LogOperation(operation = "编辑客户信息")
    public Result<SysUser> updateCustomer(@PathVariable Long id, @RequestBody SysUser user) {
        try {
            SysUser existing = userService.getById(id);
            if (existing == null) {
                return Result.error("客户不存在");
            }
            user.setUserId(id);
            userService.updateById(user);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取客户的订单列表
     */
    @GetMapping("/{id}/orders")
    public Result<List<Object>> getCustomerOrders(@PathVariable Long id) {
        // TODO: 实现获取客户订单列表的逻辑
        return Result.success(List.of());
    }

    /**
     * 更新客户标签
     */
    @PutMapping("/{id}/tag")
    public Result<Void> updateCustomerTag(@PathVariable Long id, @RequestBody TagRequest request) {
        // TODO: 实现客户标签更新逻辑（可能需要扩展用户表或创建客户信息表）
        return Result.success();
    }

    /**
     * 创建客户请求DTO
     */
    public static class CreateCustomerRequest {
        private String username;
        private String password;
        private String phone;
        private String realName;
        private String email;
        private Integer vipLevel;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Integer getVipLevel() {
            return vipLevel;
        }

        public void setVipLevel(Integer vipLevel) {
            this.vipLevel = vipLevel;
        }
    }

    /**
     * 标签请求DTO
     */
    public static class TagRequest {
        private String tag;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }
}

