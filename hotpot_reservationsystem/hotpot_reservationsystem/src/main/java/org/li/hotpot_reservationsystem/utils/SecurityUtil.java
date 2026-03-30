package org.li.hotpot_reservationsystem.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全工具类
 * 提供密码加密和获取当前用户信息的方法
 */
public class SecurityUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     * 
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return true如果密码匹配，false否则
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 获取当前登录用户的ID
     * 
     * @return 当前用户ID
     * @throws RuntimeException 如果用户未登录或用户ID格式错误
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("用户ID格式错误", e);
        }
    }

    /**
     * 获取当前认证信息
     * 
     * @return Authentication对象，如果未登录则返回null
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 检查用户是否已登录
     * 
     * @return true如果用户已登录，false否则
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
