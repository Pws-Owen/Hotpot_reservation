package org.li.hotpot_reservationsystem.common;

import lombok.Getter;

/**
 * 响应码枚举
 * 定义系统中所有可能的响应码和对应的消息
 * 用于统一管理错误码，便于维护和理解
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    
    // 认证相关 401-403
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问"),
    
    // 参数相关 400
    BAD_REQUEST(400, "请求参数错误"),
    PARAM_MISSING(4001, "缺少必要参数"),
    PARAM_INVALID(4002, "参数格式错误"),
    
    // 业务相关 4xx
    USER_NOT_FOUND(4041, "用户不存在"),
    USER_DISABLED(4042, "用户已被禁用"),
    USERNAME_EXISTS(4043, "用户名已存在"),
    PHONE_EXISTS(4044, "手机号已存在"),
    
    PASSWORD_ERROR(4011, "密码错误"),
    LOGIN_FAILED(4012, "登录失败"),
    
    RESERVATION_NOT_FOUND(4051, "预约不存在"),
    RESERVATION_STATUS_ERROR(4052, "预约状态错误"),
    TABLE_NOT_AVAILABLE(4053, "餐桌不可用"),
    
    ORDER_NOT_FOUND(4061, "订单不存在"),
    ORDER_STATUS_ERROR(4062, "订单状态错误"),
    
    MENU_ITEM_NOT_FOUND(4071, "菜品不存在"),
    MENU_ITEM_OUT_OF_STOCK(4072, "菜品库存不足"),
    
    PAYMENT_FAILED(4081, "支付失败"),
    
    // 系统相关 5xx
    SYSTEM_ERROR(500, "系统错误"),
    DATABASE_ERROR(5001, "数据库操作失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

