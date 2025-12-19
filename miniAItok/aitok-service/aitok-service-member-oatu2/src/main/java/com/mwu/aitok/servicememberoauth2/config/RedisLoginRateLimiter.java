package com.mwu.aitok.servicememberoauth2.config;



import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

//@Component
public class RedisLoginRateLimiter {

    /*
    private final StringRedisTemplate redis;
    public RedisLoginRateLimiter(StringRedisTemplate redis) { this.redis = redis; }

    // keyPrefix: e.g. "LOGIN:FAIL:USER:" or "LOGIN:FAIL:IP:"
    public boolean isAllowed(String keyPrefix, String id, int maxAttempts, Duration window, Duration lockDuration) {
        String lockKey = keyPrefix + id + ":LOCK";
        if (Boolean.TRUE.equals(redis.hasKey(lockKey))) return false;

        String counterKey = keyPrefix + id;
        Long attempts = redis.opsForValue().increment(counterKey);
        if (attempts == 1) {
            redis.expire(counterKey, window);
        }

        if (attempts != null && attempts > maxAttempts) {
            // 设置锁
            redis.opsForValue().set(lockKey, "1", lockDuration);
            // 可选：保留计数键或删除
            redis.delete(counterKey);
            return false;
        }
        return true;
    }

    public void reset(String keyPrefix, String id) {
        redis.delete(keyPrefix + id);
        redis.delete(keyPrefix + id + ":LOCK");
    }

     */

}
