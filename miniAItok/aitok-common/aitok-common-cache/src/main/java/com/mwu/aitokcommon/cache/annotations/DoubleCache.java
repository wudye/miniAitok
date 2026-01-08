package com.mwu.aitokcommon.cache.annotations;


import com.mwu.aitokcommon.cache.enums.CacheType;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author aitok
 * Caffeine+Redis二级缓存
 * 支持springEl表达式
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DoubleCache {

    String cachePrefix();

    String key() default "";    //支持springEl表达式

    long expire() default 60; // 默认1分钟

    TimeUnit unit() default TimeUnit.SECONDS;

    CacheType type() default CacheType.FULL;
}
