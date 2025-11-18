// java
package com.mwu.aitokcommon.cache.aspect;

import org.apache.commons.lang3.StringUtils;

import com.mwu.aitiokcoomon.core.utils.spring.SpElUtils;
import com.mwu.aitokcommon.cache.annotations.RedissonLock;
import com.mwu.aitokcommon.cache.service.LockService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * RedissonLockAspect
 *
 * @AUTHOR: mwu
 * @DATE: 2024/5/5
 **/
@Slf4j
@AllArgsConstructor
@Aspect
@Component
@Order(0)//确保比事务注解先执行，分布式锁在事务外
public class RedissonLockAspect {

    private LockService lockService;

    @Around("@annotation(com.mwu.aitokcommon.cache.annotations.RedissonLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
        String prefix = StringUtils.isBlank(redissonLock.prefixKey()) ? SpElUtils.getMethodKey(method) : redissonLock.prefixKey();// 默认方法限定名+注解排名（可能多个）
        String key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), redissonLock.key());
        return lockService.executeWithLockThrows(prefix + ":" + key, redissonLock.waitTime(), redissonLock.unit(), joinPoint::proceed);
    }
}
