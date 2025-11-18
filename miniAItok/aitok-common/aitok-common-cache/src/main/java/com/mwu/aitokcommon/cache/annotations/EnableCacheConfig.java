package com.mwu.aitokcommon.cache.annotations;


import com.mwu.aitokcommon.cache.aspect.DubboCacheAspect;
import com.mwu.aitokcommon.cache.aspect.RedissonLockAspect;
import com.mwu.aitokcommon.cache.config.CaffeineConfig;
import com.mwu.aitokcommon.cache.config.RedisConfig;
import com.mwu.aitokcommon.cache.service.LockService;
import com.mwu.aitokcommon.cache.service.RedisService;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EnableRedisConfig
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/30
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RedisConfig.class, CaffeineConfig.class, RedisService.class, RedissonLockAspect.class, DubboCacheAspect.class, LockService.class})
public @interface EnableCacheConfig {
}
