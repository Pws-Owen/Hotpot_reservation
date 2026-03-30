package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.ResultCode;
import org.li.hotpot_reservationsystem.entity.SysRole;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.entity.SysUserRole;
import org.li.hotpot_reservationsystem.mapper.SysRoleMapper;
import org.li.hotpot_reservationsystem.mapper.SysUserMapper;
import org.li.hotpot_reservationsystem.mapper.SysUserRoleMapper;
import org.li.hotpot_reservationsystem.service.SysUserService;
import org.li.hotpot_reservationsystem.utils.JwtUtil;
import org.li.hotpot_reservationsystem.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户Service实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new QueryWrapper<SysUser>().lambda();
        wrapper.eq(SysUser::getUsername, username);
        return this.getOne(wrapper);
    }

    @Override
    public SysUser getByPhone(String phone) {
        LambdaQueryWrapper<SysUser> wrapper = new QueryWrapper<SysUser>().lambda();
        wrapper.eq(SysUser::getPhone, phone);
        return this.getOne(wrapper);
    }

    @Override
    public String login(String username, String password) {
        SysUser user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException(ResultCode.USER_NOT_FOUND.getMessage());
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException(ResultCode.USER_DISABLED.getMessage());
        }
        if (!SecurityUtil.matches(password, user.getPassword())) {
            throw new RuntimeException(ResultCode.PASSWORD_ERROR.getMessage());
        }
        return jwtUtil.generateToken(user.getUserId(), user.getUsername());
    }

    @Override
    public SysUser register(String username, String password, String phone, String realName) {
        // 检查用户名是否已存在
        if (getByUsername(username) != null) {
            throw new RuntimeException(ResultCode.USERNAME_EXISTS.getMessage());
        }
        // 检查手机号是否已存在
        if (getByPhone(phone) != null) {
            throw new RuntimeException(ResultCode.PHONE_EXISTS.getMessage());
        }
        // 创建新用户
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(SecurityUtil.encodePassword(password));
        user.setPhone(phone);
        user.setRealName(realName);
        user.setStatus(1);
        user.setUserType(2); // 设置为客户类型
        user.setVipLevel(0); // 默认普通用户
        user.setTotalConsumption(java.math.BigDecimal.ZERO); // 初始消费金额为0
        user.setTotalOrders(0); // 初始订单数为0
        user.setPoints(0); // 初始积分为0
        this.save(user);
        return user;
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        // 查询用户角色关联
        LambdaQueryWrapper<SysUserRole> userRoleWrapper = new QueryWrapper<SysUserRole>().lambda();
        userRoleWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = userRoleMapper.selectList(userRoleWrapper);

        if (userRoles.isEmpty()) {
            return List.of();
        }

        // 获取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        // 查询角色信息
        LambdaQueryWrapper<SysRole> roleWrapper = new QueryWrapper<SysRole>().lambda();
        roleWrapper.in(SysRole::getRoleId, roleIds);
        roleWrapper.eq(SysRole::getStatus, 1); // 只查询启用的角色
        List<SysRole> roles = roleMapper.selectList(roleWrapper);

        // 返回角色编码列表
        return roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<SysUser> getCustomerPage(Long current, Long size, String name, String phone) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new QueryWrapper<SysUser>().lambda();
        
        // 只查询客户（user_type = 2）
        wrapper.eq(SysUser::getUserType, 2);
        
        if (name != null && !name.isEmpty()) {
            wrapper.like(SysUser::getRealName, name);
        }
        if (phone != null && !phone.isEmpty()) {
            wrapper.like(SysUser::getPhone, phone);
        }
        
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        // 查询用户
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException(ResultCode.USER_NOT_FOUND.getMessage());
        }
        
        // 验证当前密码
        if (!SecurityUtil.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }
        
        // 检查新密码不能与当前密码相同
        if (SecurityUtil.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("新密码不能与当前密码相同");
        }
        
        // 更新密码
        user.setPassword(SecurityUtil.encodePassword(newPassword));
        this.updateById(user);
    }

    @Override
    public PageResult<SysUser> getSystemUserPage(Long current, Long size, String username, String realName, Integer status) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new QueryWrapper<SysUser>().lambda();
        
        // 只查询系统用户（userType=1）
        wrapper.eq(SysUser::getUserType, 1);
        
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (realName != null && !realName.trim().isEmpty()) {
            wrapper.like(SysUser::getRealName, realName);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    public PageResult<SysUser> getUserPage(Long current, Long size, String username, String realName, Integer status, Integer userType) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new QueryWrapper<SysUser>().lambda();
        
        // 如果指定了userType，则筛选；否则查询所有用户
        if (userType != null) {
            wrapper.eq(SysUser::getUserType, userType);
        }
        
        if (username != null && !username.trim().isEmpty()) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (realName != null && !realName.trim().isEmpty()) {
            wrapper.like(SysUser::getRealName, realName);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = new QueryWrapper<SysUserRole>().lambda();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = userRoleMapper.selectList(wrapper);
        return userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }
}

