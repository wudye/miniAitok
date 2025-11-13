package com.mwu.aitok.gateway.config;



import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/*
要点提醒（简短）：
- 分桶会降低精确度（不同用户可能共享桶），适合非严格按用户隔离的场景。根据用户量与允许误差选择 BUCKET_COUNT。
- TTL 刷新最好只针对使用到的 key 做延长，避免频繁全表操作；SC​AN 比 KEYS 安全，生产环境要限制扫描速率与批量大小。
- 可结合 Redis 的 maxmemory + eviction（如 volatile-lru）作为兜底方案，并监控 Redis 内存与 key 数增长。

如果你的限流逻辑在写入/创建限流 key 时就原子地设置了 TTL（例如 expire-after-write: 7200 能保证第一次写入时带上过期时间），就不需要额外用定时任务去扫描并刷新/清理 Redis。关键点是保证「在 key 第一次被创建时」就带上 TTL 或以原子方式在创建后立即设置 TTL，否则可能出现永久残留的 key 或竞态。
下面给出两种推荐实现（简短说明后给出代码）：
方案 A（推荐）：用 Lua 脚本在一次原子操作中 INCR 并在值为 1 时设置 EXPIRE，避免竞态。
方案 B（备选，简单）：先 INCR，再检测返回值为 1 时设置 EXPIRE（有极小竞态风险，但通常可接受）。
// language: java
// File: `gateway-server-8080/src/main/java/com/mwu/gataway/config/RateLimiterKeyService.java`
package com.mwu.gataway.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service

 */
public class RateLimiterKeyService {

    /*
    private final StringRedisTemplate redisTemplate;
    // Lua: INCR key; if value == 1 then EXPIRE key ttl end; return value
    private static final String INCR_AND_EXPIRE_LUA =
            "local v = redis.call('INCR', KEYS[1]) " +
            "if tonumber(v) == 1 then " +
            "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "return v";

    public RateLimiterKeyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 方案 A：原子操作（推荐）
    public Long incrAndEnsureTtl(String key, Duration ttl) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(INCR_AND_EXPIRE_LUA, Long.class);
        // ARGV[1] 要传秒数
        return redisTemplate.execute(script, Collections.singletonList(key), String.valueOf(ttl.getSeconds()));
    }
     */


}


/*
优先采用「在写入时原子设置 TTL（expire-after-write 或 Lua INCR+EXPIRE）」的方案，
删除定时扫描组件；如果不能立即保证写入带 TTL，则暂时保留该组件直到完成改造


 redis-rate-limiter.expire-after-write: 7200 放在 application.yml 意味着希望在限流器在 Redis 写入 key 时同时给这些 key 设一个 7200 秒的 TTL。若限流实现确实在首次写入时带上 TTL，Redis 会在 TTL 到期后自动移除该 key，无需你的定时任务去清理。
Redis 如何删除到期 key（两种机制）：
被动删除（lazy）：当某个客户端访问一个 key 时，Redis 会检查该 key 是否已过期，若过期则删除并当作不存在返回。
主动删除（active）：Redis 后台周期性运行过期扫描，随机抽样一些带 TTL 的 key 并删除已经过期的。主动扫描并非遍历所有 key，而是采样/重复采样以控制开销，所以删除可能有短暂延迟，不能保证在 TTL 精确到秒级瞬时删除。
频率与延迟：Redis 的后台周期（server cron）大约每 100ms 执行一次若干维护任务，其中包含主动过期逻辑；过期扫描次数/力度会根据配置（如 hz、active-expire-effort 等）自适应，普通情况下过期延迟很小但不是严格实时。
你的定时任务作用与代价：你每 5 分钟用 SCAN 把 key 找出来并刷新 TTL（或删除）是一个「兜底」方案，适用于限流写入不保证带 TTL 的情况。但代价包括 SCAN 开销、网络与写操作负担，以及可能把大量不活跃 key 再次延长 TTL（如果刷新策略不慎）。如果 expire-after-write 生效，建议删除该定时任务；否则应改为在写入时用原子 INCR+EXPIRE（Lua）或确保限流组件设置 TTL。
如何验证：
发起一次限流请求产生 key。
在 Redis 上运行 TTL <key>（或 pttl）检查返回值：正数=剩余秒数，-1=无 TTL，-2=key 不存在。
若 TTL 正确为约 7200 秒，则 expire-after-write 生效，可删定时任务；若返回 -1，说明写入时并未设置 TTL，需要保留或改造写入逻辑。
 */