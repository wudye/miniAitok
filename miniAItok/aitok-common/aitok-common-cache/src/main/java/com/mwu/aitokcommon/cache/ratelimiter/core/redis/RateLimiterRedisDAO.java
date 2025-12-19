package com.mwu.aitokcommon.cache.ratelimiter.core.redis;

import lombok.AllArgsConstructor;
import org.redisson.api.*;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 限流 Redis DAO
 */
@AllArgsConstructor
public class RateLimiterRedisDAO {

    /**
     * 限流操作
     *
     * KEY 格式：rate_limiter:%s // 参数为 uuid
     * VALUE 格式：String
     * 过期时间：不固定
     */
    private static final String RATE_LIMITER = "rate_limiter:%s";

    private final RedissonClient redissonClient;

    public Boolean tryAcquire(String key, int count, int time, TimeUnit timeUnit) {
        // 1. 获得 RRateLimiter，并设置 rate 速率
        RRateLimiter rateLimiter = getRRateLimiter(key, count, time, timeUnit);
        // 2. 作用：尝试从限流器获取1个许可 tryAcquire() （非阻塞）
       // rateLimiter.acquire();  // 会阻塞直到获得令牌
        // 不适合Web接口，会导致请求阻塞
        /*
        请求到达
            ↓
        tryAcquire() 尝试获取许可
            ↓
        检查令牌桶
            ↓
        ┌─ 有令牌？ ── 是 ──→ 消耗1个令牌 ──→ return true (允许)
        │
        │
        └─ 否 ──→ return false (拒绝)

        假设配置：每分钟最多10次请求

        // 令牌桶状态（容量=10）
        初始: 🔸🔸🔸🔸🔸🔸🔸🔸🔸🔸  (10个令牌)

        第1次请求: tryAcquire() → 🔸🔸🔸🔸🔸🔸🔸🔸🔸  (消耗1个) → true
        第2次请求: tryAcquire() → 🔸🔸🔸🔸🔸🔸🔸🔸    (消耗1个) → true
        ...
        第10次请求: tryAcquire() → 🔸                    (消耗1个) → true
        第11次请求: tryAcquire() → 空桶               (无令牌) → false ❌

         */
        return rateLimiter.tryAcquire();
    }

    /*
            Key 格式示例：
        rate_limiter:user:123 - 用户限流
        rate_limiter:ip:192.168.1.1 - IP限流
        rate_limiter:api:/chat/stream - 接口限流
     */
    private static String formatKey(String key) {
        return String.format(RATE_LIMITER, key);
    }

    private RRateLimiter getRRateLimiter(String key, long count, int time, TimeUnit timeUnit) {
        String redisKey = formatKey(key);
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(redisKey);
        long rateInterval = timeUnit.toSeconds(time);
        // 1. 如果不存在，设置 rate 速率
        RateLimiterConfig config = rateLimiter.getConfig();
        if (config == null) {
            rateLimiter.trySetRate(RateType.OVERALL, count, rateInterval, RateIntervalUnit.SECONDS);
            return rateLimiter;
        }
        // 2. 如果存在，并且配置相同，则直接返回
        if (config.getRateType() == RateType.OVERALL
                && Objects.equals(config.getRate(), count)
                && Objects.equals(config.getRateInterval(), TimeUnit.SECONDS.toMillis(rateInterval))) {
            return rateLimiter;
        }
        // 3. 如果存在，并且配置不同，则进行新建
        rateLimiter.setRate(RateType.OVERALL, count, rateInterval, RateIntervalUnit.SECONDS);
        return rateLimiter;
    }

}
