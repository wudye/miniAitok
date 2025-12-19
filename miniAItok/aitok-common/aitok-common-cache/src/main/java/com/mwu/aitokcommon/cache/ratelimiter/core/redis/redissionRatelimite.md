Redisson RRateLimiter 详解

限流算法原理
    Redisson 使用令牌桶算法
    令牌桶（容量=10）
    🔸🔸🔸🔸🔸🔸🔸🔸🔸🔸
    ↓ ↓ ↓ ↓ ↓
    请求消耗令牌
    ↓
    令牌不足 → 限流
    ↓
    定时补充令牌
1. 什么是 RRateLimiter
   RRateLimiter 是 Redisson 提供的分布式限流器，基于 Redis 实现的令牌桶算法。
   // Redisson 的限流器
   RRateLimiter rateLimiter = redissonClient.getRateLimiter("my_limiter");
   rateLimiter.trySetRate(RateType.OVERALL, 10, 1, RateIntervalUnit.SECONDS);
   boolean acquired = rateLimiter.tryAcquire(); // 尝试获取许可

2. RRateLimiter 的优势
   ✅ 分布式一致性
   // 多个服务实例共享同一个限流器
   // 实例A: rateLimiter.tryAcquire()  // 成功
   // 实例B: rateLimiter.tryAcquire()  // 可能失败（因为A已消耗）
   ✅ 原子性操作
   基于 Redis 的原子命令
   避免竞态条件
   保证计数准确性
   ✅ 配置智能管理
   自动检测现有配置
   支持动态更新
   避免重复设置

3. 使用原生 Redis 实现限流器
   如果不使用 Redisson，可以用原生 Redis 命令实现限流。

方案1：使用 Lua 脚本（推荐）
@Repository
public class RedisRateLimiterDAO {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // Lua 脚本：原子性限流检查
    private static final String RATE_LIMIT_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local tokens = tonumber(ARGV[2])
        local interval = tonumber(ARGV[3])
        local request_time = tonumber(ARGV[4])
        
        local current_time = redis.call('TIME')
        current_time = tonumber(current_time[1]) * 1000 + tonumber(current_time[2]) / 1000
        
        local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
        local current_tokens = tonumber(bucket[1]) or capacity
        local last_refill = tonumber(bucket[2]) or current_time
        
        -- 计算需要补充的令牌数
        local time_passed = current_time - last_refill
        local tokens_to_add = math.floor((time_passed / interval) * tokens)
        current_tokens = math.min(capacity, current_tokens + tokens_to_add)
        
        -- 尝试消耗令牌
        if current_tokens >= 1 then
            current_tokens = current_tokens - 1
            redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', current_time)
            redis.call('EXPIRE', key, math.ceil(interval * 2))
            return 1
        else
            redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', current_time)
            redis.call('EXPIRE', key, math.ceil(interval * 2))
            return 0
        end
        """;
    
    public Boolean tryAcquire(String key, int capacity, int tokens, long interval) {
        Long result = redisTemplate.execute(
            (RedisCallback<Long>) connection -> 
                connection.eval(RATE_LIMIT_SCRIPT.getBytes(), 
                    ReturnType.INTEGER, 1,
                    key.getBytes(),
                    String.valueOf(capacity).getBytes(),
                    String.valueOf(tokens).getBytes(),
                    String.valueOf(interval).getBytes(),
                    String.valueOf(System.currentTimeMillis()).getBytes())
        );
        
        return result != null && result == 1;
    }
}
方案2：使用 Redis INCR + EXPIRE（简单版）
@Repository
public class SimpleRedisRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;
    
    public Boolean tryAcquire(String key, int limit, long windowSeconds) {
        // 使用 Redis 的 INCR 命令原子性递增
        Long current = redisTemplate.opsForValue().increment(key);
        
        if (current == 1) {
            // 第一次设置，添加过期时间
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        
        return current <= limit;
    }
}
方案3：使用滑动窗口（更精确）
@Repository
public class SlidingWindowRateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;
    
    public Boolean tryAcquire(String key, int limit, long windowSeconds) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSeconds * 1000;
        
        // 使用 ZSet 实现滑动窗口
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        
        Long count = redisTemplate.opsForZSet().count(key, windowStart, now);
        
        if (count < limit) {
            redisTemplate.opsForZSet().add(key, UUID.randomUUID().toString(), now);
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
            return true;
        }
        
        return false;
    }
}

方案对比
特性	Redisson RRateLimiter	原生 Redis + Lua	简单 INCR	滑动窗口
实现复杂度	低	中	低	高
性能	优秀	良好	一般	一般
精度	高	高	中	最高
分布式安全	✅	✅	✅	✅
功能丰富	✅	⚠️	❌	⚠️
维护成本	低	中	低	高