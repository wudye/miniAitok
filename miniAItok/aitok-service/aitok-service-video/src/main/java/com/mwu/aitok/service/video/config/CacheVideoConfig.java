package com.mwu.aitok.service.video.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Data
@Configuration
@ConfigurationProperties("cache-config")
public class CacheVideoConfig {

    private Map<String, CacheSpec> specs;

    @Data
    public static class CacheSpec {
        private Integer expireTime;
        private Integer maxSize;
    }

    /*
    Ticker 是 Caffeine 提供的一个时间源抽象（类型为 com.github.benmanes.caffeine.cache.Ticker）。Caffeine 在做基于时间的失效（如 expireAfterWrite / expireAfterAccess）时不会直接调用 System.nanoTime()，而是通过 Ticker 查询当前时间（以纳秒为单位）。这样可以：
在生产中使用 Ticker.systemTicker()（基于 System.nanoTime()）。
在单元测试中注入自定义的 Ticker，可精确控制“时间流逝”，便于测试缓存的过期行为。
     */
    @Bean
    public CacheManager cacheManager(Ticker ticker) {
        SimpleCacheManager manager = new SimpleCacheManager();
        /*
           // 定义多个命名缓存
        List<CaffeineCache> caches = List.of(
            buildCache("videos", new CacheSpec(30, 1000), ticker),
            buildCache("users", new CacheSpec(60, 500), ticker),
            buildCache("debug", new CacheSpec(10, 100), ticker)  // 调试用缓存
        );

         */
        if (specs != null) {
            List<CaffeineCache> caches = specs.entrySet().stream()
                    .map(entry -> buildCache(entry.getKey(), entry.getValue(), ticker))
                    .collect(Collectors.toList());
            manager.setCaches(caches);
        }
        return manager;
    }

    private CaffeineCache buildCache(String name, CacheVideoConfig.CacheSpec cacheSpec, Ticker ticker) {
        final Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .expireAfterWrite(cacheSpec.getExpireTime(), TimeUnit.MINUTES)
                .maximumSize(cacheSpec.getMaxSize())
                .ticker(ticker);
        return new CaffeineCache(name, caffeineBuilder.build());
    }

    // ticker 方法定义了一个 Ticker Bean，返回系统默认的时间源，用于支持缓存的时间控制
    @Bean
    public Ticker ticker() {
        return Ticker.systemTicker();
    }
}
