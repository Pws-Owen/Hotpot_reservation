package org.li.hotpot_reservationsystem.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理Controller层抛出的异常，返回统一的错误响应格式
 * 按异常类型优先级从高到低处理：RuntimeException -> Exception
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理RuntimeException（业务异常）
     * 处理业务逻辑中抛出的运行时异常，将异常消息返回给前端
     * 
     * @param e RuntimeException异常对象
     * @return Result对象，包含错误信息
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Object> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理其他所有异常
     * 处理系统异常（如数据库连接异常、空指针异常等），
     * 返回通用错误信息，避免暴露系统内部错误详情给用户
     * 
     * @param e Exception异常对象
     * @return Result对象，包含通用错误信息
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        log.error("系统异常", e);
        // 避免暴露系统内部错误信息给用户
        return Result.error("系统异常，请联系管理员");
    }
}

