package org.li.hotpot_reservationsystem.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 * 使用LogAspect切面自动拦截标记了此注解的方法并记录日志
 * 
 * 使用示例：
 * @LogOperation(operation = "创建订单")
 * public Result createOrder(...) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {
    /**
     * 操作描述（与operation功能相同，保留用于兼容）
     */
    String value() default "";

    /**
     * 操作类型描述（如："创建订单"、"删除用户"等）
     * 如果不指定，将使用方法名作为操作描述
     */
    String operation() default "";
}

