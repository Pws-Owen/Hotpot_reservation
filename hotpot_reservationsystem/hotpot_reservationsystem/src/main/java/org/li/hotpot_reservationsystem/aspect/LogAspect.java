package org.li.hotpot_reservationsystem.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.li.hotpot_reservationsystem.annotation.LogOperation;
import org.li.hotpot_reservationsystem.entity.SysLog;
import org.li.hotpot_reservationsystem.service.SysLogService;
import org.li.hotpot_reservationsystem.service.SysUserService;
import org.li.hotpot_reservationsystem.entity.SysUser;
import org.li.hotpot_reservationsystem.utils.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面
 * 使用AOP技术自动记录系统中标记了@LogOperation注解的方法的操作日志
 * 记录内容包括：用户信息、操作类型、请求参数、IP地址、执行时间、操作结果等
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Autowired
    private SysLogService logService;

    @Autowired
    private SysUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切点：拦截所有Controller中带有@LogOperation注解的方法
     */
    @Pointcut("@annotation(org.li.hotpot_reservationsystem.annotation.LogOperation)")
    public void logPointcut() {
    }

    /**
     * 环绕通知：记录操作日志
     * 在方法执行前后记录日志信息，包括执行时间和操作结果
     * 
     * @param point 连接点（被拦截的方法）
     * @return 方法执行结果
     * @throws Throwable 方法执行时抛出的异常
     */
    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysLog log = new SysLog();
        
        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
            
            // 获取方法签名和注解
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            LogOperation logOperation = method.getAnnotation(LogOperation.class);
            
            // 获取操作描述
            String operation = logOperation != null && !logOperation.operation().isEmpty() 
                    ? logOperation.operation() 
                    : (logOperation != null && !logOperation.value().isEmpty() 
                            ? logOperation.value() 
                            : method.getName());
            
            // 获取用户信息
            Authentication authentication = SecurityUtil.getAuthentication();
            Long userId = null;
            String username = null;
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                try {
                    userId = Long.valueOf(authentication.getName());
                    SysUser user = userService.getById(userId);
                    if (user != null) {
                        username = user.getUsername();
                    }
                } catch (Exception e) {
                    // 忽略获取用户信息失败
                }
            }
            
            // 获取请求参数
            Object[] args = point.getArgs();
            String params = "";
            if (args != null && args.length > 0) {
                try {
                    // 过滤掉HttpServletRequest和HttpServletResponse等参数
                    Object[] filteredArgs = Arrays.stream(args)
                            .filter(arg -> arg != null && 
                                    !arg.getClass().getName().startsWith("jakarta.servlet") &&
                                    !arg.getClass().getName().startsWith("org.springframework.web"))
                            .toArray();
                    if (filteredArgs.length > 0) {
                        params = objectMapper.writeValueAsString(filteredArgs);
                        // 限制参数长度，避免过长
                        if (params.length() > 2000) {
                            params = params.substring(0, 2000) + "...";
                        }
                    }
                } catch (Exception e) {
                    params = "参数序列化失败";
                }
            }
            
            // 设置日志基本信息
            log.setUserId(userId);
            log.setUsername(username);
            log.setOperation(operation);
            log.setMethod(request != null ? request.getMethod() + " " + request.getRequestURI() : method.getName());
            log.setParams(params);
            log.setIp(getIpAddress(request));
            log.setLocation("未知"); // 可以根据IP地址查询地理位置，这里简化处理
            log.setStatus(1); // 默认成功，如果抛出异常会在catch中设置为失败
            
            // 执行方法
            Object result = point.proceed();
            
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            log.setExecutionTime(executionTime);
            log.setStatus(1); // 成功
            
            // 保存日志（异步保存，避免影响主流程）
            saveLog(log);
            
            return result;
        } catch (Throwable e) {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            log.setExecutionTime(executionTime);
            log.setStatus(0); // 失败
            log.setErrorMsg(e.getMessage());
            
            // 限制错误信息长度
            if (log.getErrorMsg() != null && log.getErrorMsg().length() > 1000) {
                log.setErrorMsg(log.getErrorMsg().substring(0, 1000) + "...");
            }
            
            // 保存日志
            saveLog(log);
            
            // 重新抛出异常
            throw e;
        }
    }

    /**
     * 保存日志
     */
    private void saveLog(SysLog log) {
        try {
            // 在新线程中保存，避免阻塞主流程
            new Thread(() -> {
                try {
                    logService.save(log);
                } catch (Exception e) {
                    // 日志保存失败不影响主流程，记录错误日志
                    logger.error("保存操作日志失败", e);
                }
            }).start();
        } catch (Exception e) {
            // 忽略异常，避免影响主流程
        }
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }
}

