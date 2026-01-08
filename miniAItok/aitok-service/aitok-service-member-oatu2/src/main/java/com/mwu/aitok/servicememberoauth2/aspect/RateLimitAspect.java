package com.mwu.aitok.servicememberoauth2.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitok.servicememberoauth2.annotation.RateLimit;
import com.mwu.aitokcommon.cache.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint point, RateLimit rateLimit) throws Throwable {
        String key = getLimitKey(point, rateLimit);
        
        try {
            // 尝试获取计数
            Object countObj = redisService.getCacheObject(key);
            if (countObj == null) {
                // 第一次访问，设置计数为1并过期
                redisService.setCacheObject(key, 1L, rateLimit.window(), TimeUnit.SECONDS);
            } else {
                // 处理可能的Integer类型
                long count = countObj instanceof Integer ? ((Integer) countObj).longValue() : (Long) countObj;
                if (count < rateLimit.count()) {
                    // 增加计数
                    redisService.setCacheObject(key, count + 1, rateLimit.window(), TimeUnit.SECONDS);
                } else {
                    // 超过限制
                    throw new RuntimeException("请求过于频繁，请稍后重试");
                }
            }
            
            return point.proceed();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取限流key
     */
    private String getLimitKey(ProceedingJoinPoint point, RateLimit rateLimit) {
        StringBuilder key = new StringBuilder(rateLimit.prefix());
        
        // 获取方法名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        key.append(method.getDeclaringClass().getSimpleName()).append(":").append(method.getName()).append(":");
        
        // 根据类型添加标识
        switch (rateLimit.type()) {
            case IP:
                key.append(getClientIP());
                break;
            case USER:
                key.append(getUserId());
                break;
            case GLOBAL:
                // 全局限流不需要额外标识
                break;
        }
        
        return key.toString();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIP() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 获取当前用户ID（需要从JWT token或SecurityContext中获取）
     */
    private String getUserId() {
        try {
            // 这里需要根据你的认证方式获取用户ID
            // 例如从JWT token或Spring Security Context中获取
            return "anonymous"; // 临时实现，需要根据实际情况修改
        } catch (Exception e) {
            return "anonymous";
        }
    }
}