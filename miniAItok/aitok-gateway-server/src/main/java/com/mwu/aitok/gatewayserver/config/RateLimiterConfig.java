// language: java
package com.mwu.aitok.gatewayserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class RateLimiterConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterConfig.class);
    private static final int BUCKET_COUNT = 1024;

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String header = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (header != null && !header.isBlank()) {
                String key = computeKey(header);
                log.debug("Resolved rate-limiter key from header -> {}", key);
                return Mono.just(key);
            }

            return ReactiveSecurityContextHolder.getContext()
                    .map(SecurityContext::getAuthentication)
                    .map(auth -> {
                        Object principal = auth.getPrincipal();
                        if (principal instanceof Jwt) {
                            Jwt jwt = (Jwt) principal;
                            String uid = jwt.getClaimAsString("userid");
                            return uid != null && !uid.isBlank() ? uid : jwt.getSubject();
                        }
                        return auth.getName();
                    })
                    .defaultIfEmpty("anonymous")
                    .map(raw -> {
                        String key = computeKey(raw);
                        log.debug("Resolved rate-limiter key from JWT/auth -> {}", key);
                        return key;
                    });
        };
    }

    private String computeKey(String raw) {
        String input = (raw == null || raw.isBlank()) ? "anonymous" : raw;
        int bucket = Math.abs(hash(input)) % BUCKET_COUNT;
        return "user-bucket:" + bucket;
    }

    private int hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            int h = ((digest[0] & 0xFF) << 24) | ((digest[1] & 0xFF) << 16)
                    | ((digest[2] & 0xFF) << 8) | (digest[3] & 0xFF);
            return h;
        } catch (NoSuchAlgorithmException e) {
            return s.hashCode();
        }
    }
}
