package com.mwu.aitok.gatewayserver.config;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Map;
/*
@Component
public class UserHeaderFilter implements GlobalFilter, Ordered {

    private static final String HDR_USER_ID = "X-User-Id";
    private static final String HDR_USERNAME = "X-Username";
    private static final String HDR_ROLES = "X-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(pr -> pr instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(jwtAuth -> {
                    Map<String, Object> claims = jwtAuth.getToken().getClaims();
                    Object userIdObj = claims.getOrDefault("userId", jwtAuth.getName());
                    Object usernameObj = claims.getOrDefault("username", jwtAuth.getName());
                    String userId = userIdObj == null ? "" : String.valueOf(userIdObj);
                    String username = usernameObj == null ? "" : String.valueOf(usernameObj);;
                    // 可选：从 claim 或 authorities 构造 roles 字符串
                    String roles = jwtAuth.getAuthorities().stream()
                            .map(Object::toString)
                            .reduce((a,b)->a + "," + b)
                            .orElse(null);

                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                            .header(HDR_USER_ID, userId)
                            .header(HDR_USERNAME, username)
                            .header(HDR_ROLES, roles == null ? "" : roles)
                            .build();
                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutated).build();
                    return chain.filter(mutatedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}


 */