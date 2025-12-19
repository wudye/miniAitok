package com.mwu.aitokcommon.cache.ratelimiter.core.keyresolver;

import com.mwu.aitokcommon.cache.ratelimiter.core.annotation.RateLimiter;
import org.aspectj.lang.JoinPoint;

/**
 * 限流 Key 解析器接口
 */
public interface RateLimiterKeyResolver {

    /**
     * 解析一个 Key
     *
     * @param rateLimiter 限流注解
     * @param joinPoint  AOP 切面
     * @return Key
     */
    String resolver(JoinPoint joinPoint, RateLimiter rateLimiter);

}
