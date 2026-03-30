package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.entity.SysUser;

import java.util.List;

/**
 * 用户Service接口
 */
public interface SysUserService extends IService<SysUser> {
    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);

    /**
     * 根据手机号查询用户
     */
    SysUser getByPhone(String phone);

    /**
     * 登录
     */
    String login(String username, String password);

    /**
     * 注册
     */
    SysUser register(String username, String password, String phone, String realName);

    /**
     * 根据用户ID获取角色编码列表
     */
    List<String> getRoleCodesByUserId(Long userId);

    /**
     * 分页查询客户列表（普通用户）
     */
    org.li.hotpot_reservationsystem.common.PageResult<SysUser> getCustomerPage(Long current, Long size, String name, String phone);
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String currentPassword, String newPassword);

    /**
     * 分页查询系统用户列表（userType=1）
     */
    org.li.hotpot_reservationsystem.common.PageResult<SysUser> getSystemUserPage(Long current, Long size, String username, String realName, Integer status);

    /**
     * 分页查询用户列表（所有用户，可筛选userType）
     */
    org.li.hotpot_reservationsystem.common.PageResult<SysUser> getUserPage(Long current, Long size, String username, String realName, Integer status, Integer userType);

    /**
     * 根据用户ID获取角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long userId);
}

