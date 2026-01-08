package com.mwu.aitokcommon.cache.aspect;

import com.github.benmanes.caffeine.cache.Cache;

import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitokcommon.cache.annotations.DoubleCache;
import com.mwu.aitokcommon.cache.constant.CacheConstant;
import com.mwu.aitokcommon.cache.enums.CacheType;
import com.mwu.aitokcommon.cache.util.ElParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.TreeMap;

@Slf4j
@AllArgsConstructor
@Aspect
@Component
public class DubboCacheAspect {

    private Cache<String, Object> cache;     // Caffeine本地缓存
    private RedisTemplate<String, Object> redisTemplate;

    /*
    1. @Pointcut 方法定义
        作用: 定义切点，即告诉Spring哪些方法需要被拦截

        @Pointcut : 声明这是一个切点定义
        @annotation(...) : 匹配标注了特定注解的方法
        cacheAspect() : 方法名，作为切点的标识符（方法体为空）

     */
    @Pointcut("@annotation(com.mwu.aitokcommon.cache.annotations.DoubleCache)")
    public void cacheAspect() {
    }

    /*
     @Around 方法定义
     定义通知，即切点匹配到的方法执行时的具体逻辑

        @Around : 环绕通知，可以在方法执行前后添加逻辑
        "cacheAspect()" : 引用上面定义的切点
        doAround() : 具体的增强逻辑
     */
    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();// 获取方法参数名
        Method method = signature.getMethod();

        //拼接解析springEl表达式的map
        String[] paramNames = signature.getParameterNames();
        Object[] args = point.getArgs();
        TreeMap<String, Object> treeMap = new TreeMap<>();
        
        // 添加空值检查
        if (paramNames != null && args != null) {
            for (int i = 0; i < Math.min(paramNames.length, args.length); i++) {
                if (paramNames[i] != null) {
                    treeMap.put(paramNames[i], args[i]);
                }
            }
        }
        /*
        方法: getUser(@RequestParam("id") Long id)
        注解: @DoubleCache(key = "#id")
        结果: elResult = "123"
         */

        DoubleCache annotation = method.getAnnotation(DoubleCache.class);
        String realKey = annotation.cachePrefix();
        if (!StringUtils.isEmpty(annotation.key())) {
            String elResult = ElParser.parse(annotation.key(), treeMap);
            realKey = annotation.cachePrefix() + CacheConstant.COLON + elResult;
        }
        //强制更新
        if (annotation.type() == CacheType.PUT) {
            Object object = point.proceed();
            try {
                redisTemplate.opsForValue().set(realKey, object, annotation.expire(), annotation.unit());
                cache.put(realKey, object);
            } catch (Exception e) {
                log.warn("Cache PUT operation failed for key: {}", realKey, e);
            }
            return object;
        }
        //删除
        else if (annotation.type() == CacheType.DELETE) {
            try {
                redisTemplate.delete(realKey);
                cache.invalidate(realKey);
            } catch (Exception e) {
                log.warn("Cache DELETE operation failed for key: {}", realKey, e);
            }
            return point.proceed();
        }

        //读写，查询Caffeine  查询Caffeine（最快）
        Object caffeineCache = null;
        try {
            caffeineCache = cache.getIfPresent(realKey);
        } catch (Exception e) {
            log.warn("Caffeine cache read failed for key: {}", realKey, e);
        }
        
        if (Objects.nonNull(caffeineCache)) {
            log.info("get data from caffeine");
            return caffeineCache;
        }

        //查询Redis
        Object redisCache = null;
        try {
            redisCache = redisTemplate.opsForValue().get(realKey);
            // 检查反序列化结果是否为LinkedHashMap（类型丢失）
            if (redisCache instanceof java.util.LinkedHashMap) {
                log.warn("Redis cache type loss detected for key: {}, expected type may be lost during deserialization", realKey);
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed for key: {}", realKey, e);
        }
        
        if (Objects.nonNull(redisCache)) {
            log.info("get data from redis");
            try {
                cache.put(realKey, redisCache);
            } catch (Exception e) {
                log.warn("Caffeine cache write failed for key: {}", realKey, e);
            }
            return redisCache;
        }

        log.info("get data from database");
        Object object = point.proceed();
        if (Objects.nonNull(object)) {
            try {
                //写入Redis
                redisTemplate.opsForValue().set(realKey, object, annotation.expire(), annotation.unit());
                //写入Caffeine
                cache.put(realKey, object);
                
                // 调试日志：验证缓存写入
                log.info("✅ 缓存写入成功 - Key: {}, Value: {}, TTL: {} {}", 
                        realKey, object, annotation.expire(), annotation.unit());
                
                // 验证Redis中的值
                Object redisValue = redisTemplate.opsForValue().get(realKey);
                log.info("🔴 Redis验证 - Key: {}, Value: {}", realKey, redisValue);
                
                // 验证Caffeine中的值
                Object caffeineValue = cache.getIfPresent(realKey);
                log.info("⚡ Caffeine验证 - Key: {}, Value: {}", realKey, caffeineValue);
                
            } catch (Exception e) {
                log.warn("Cache write operation failed for key: {}", realKey, e);
            }
        }
        return object;
    }
}
