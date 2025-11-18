package com.mwu.aitok.gateway.config;


import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// 你还需要创建一个自定义的 KeyResolver Bean，例如基于用户 IP 的限流：
// 大量唯一 key 会增加 Redis 存储与过期开销，注意设计 TTL 和清理策略
@Configuration
public class RateLimiterConfig {

    private static final int BUCKET_COUNT = 1024; // 根据容量调整


    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            String raw = (userId != null && !userId.isEmpty())
                    ? userId
                    : (exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown");
            int bucket = Math.abs(hash(raw)) % BUCKET_COUNT;
            return Mono.just("user-bucket:" + bucket);
        };
    }

    private int hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            // 取前4字节构成 int
            int h = ((digest[0] & 0xFF) << 24) | ((digest[1] & 0xFF) << 16)
                    | ((digest[2] & 0xFF) << 8) | (digest[3] & 0xFF);
            return h;
        } catch (NoSuchAlgorithmException e) {
            return s.hashCode();
        }
    }
}
