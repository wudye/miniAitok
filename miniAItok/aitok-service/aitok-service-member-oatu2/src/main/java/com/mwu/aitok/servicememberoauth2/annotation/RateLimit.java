package com.mwu.aitok.servicememberoauth2.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 限流key前缀
     */
    String prefix() default "RATE_LIMIT:";
    
    /**
     * 限流时间窗口（秒）
     */
    long window() default 60;
    
    /**
     * 限流次数
     */
    int count() default 5;
    
    /**
     * 限流类型：IP、USER、GLOBAL
     */
    LimitType type() default LimitType.IP;
    
    enum LimitType {
        IP,     // 按IP限流
        USER,   // 按用户限流
        GLOBAL  // 全局限流
    }
}