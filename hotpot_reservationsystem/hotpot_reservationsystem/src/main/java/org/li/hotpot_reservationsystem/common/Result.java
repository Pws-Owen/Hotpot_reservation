package org.li.hotpot_reservationsystem.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果类
 * 用于统一封装API响应结果，包含响应码、消息、数据和时间戳
 * 
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 响应码（200表示成功，其他表示失败）
     */
    private Integer code;

    /**
     * 响应消息（成功或错误的提示信息）
     */
    private String message;

    /**
     * 响应数据（泛型，可以是任意类型）
     */
    private T data;

    /**
     * 响应时间戳（毫秒）
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应（无数据）
     * 
     * @param <T> 响应数据类型
     * @return Result对象，code=200
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     * 
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return Result对象，code=200，包含响应数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     * 
     * @param message 自定义成功消息
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return Result对象，code=200，包含自定义消息和响应数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应（使用默认错误码和消息）
     * 
     * @param <T> 响应数据类型
     * @return Result对象，code=500
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage(), null);
    }

    /**
     * 失败响应（自定义错误消息）
     * 
     * @param message 错误消息
     * @param <T> 响应数据类型
     * @return Result对象，code=500，包含自定义错误消息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message, null);
    }

    /**
     * 失败响应（自定义错误码和消息）
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 响应数据类型
     * @return Result对象，包含自定义错误码和消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应（使用ResultCode枚举）
     * 
     * @param resultCode 结果码枚举
     * @param <T> 响应数据类型
     * @return Result对象，包含ResultCode中的错误码和消息
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
}

