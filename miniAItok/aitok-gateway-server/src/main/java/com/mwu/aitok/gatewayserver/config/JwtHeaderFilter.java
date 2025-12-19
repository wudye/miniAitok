// java
package com.mwu.aitok.gatewayserver.config;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
/*
@Component
public class JwtHeaderFilter implements GlobalFilter, Ordered {

    private static final String HEADER_NAME = "X-User-Id";
    private static final String HDR_USERNAME = "X-Username";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 已存在则跳过，避免重复执行
        if (exchange.getRequest().getHeaders().containsKey(HEADER_NAME)) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    String userId = null;
                    String userName = null;

                    Object principal = auth.getPrincipal();
                    if (principal instanceof Jwt) {
                        Jwt jwt = (Jwt) principal;
                        userId = jwt.getClaimAsString("userid");
                        userName = jwt.getClaimAsString("username");
                        if (userId == null) userId = jwt.getSubject();
                    } else if (auth.getCredentials() instanceof Jwt) {
                        Jwt jwt = (Jwt) auth.getCredentials();
                        userId = jwt.getClaimAsString("userid");
                        userName = jwt.getClaimAsString("username");
                        if (userId == null) userId = jwt.getSubject();
                    } else {
                        userId = auth.getName();
                    }

                    if (userId == null) {
                        return chain.filter(exchange);
                    }

                    ServerHttpRequest mutated = exchange.getRequest()
                            .mutate()
                            .header(HEADER_NAME, userId)
                            .header(HDR_USERNAME, userName == null ? "" : userName)
                            .build();

                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutated).build();
                    return chain.filter(mutatedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        // 早于大部分过滤器执行，确保在 RequestRateLimiter 等前完成请求头注入
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
*/