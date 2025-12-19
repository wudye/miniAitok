package com.mwu.aitok.gateway.config;


import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@Component
//@Order(Ordered.LOWEST_PRECEDENCE)
public class JwtHeaderFilter implements WebFilter {

    private static final String HEADER_NAME = "X-User-Id";
// java
// 将下面的方法体替换到上面类中，或使用下面完整类替换上面不完整的实现：

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> {
                    String userId = null;
                    Object principal = auth.getPrincipal();
                    if (principal instanceof Jwt) {
                        Jwt jwt = (Jwt) principal;
                        userId = jwt.getClaimAsString("username");
                        if (userId == null) userId = jwt.getSubject();
                    } else if (auth.getCredentials() instanceof Jwt) {
                        Jwt jwt = (Jwt) auth.getCredentials();
                        userId = jwt.getClaimAsString("username");
                        if (userId == null) userId = jwt.getSubject();
                    } else {
                        userId = auth.getName();
                    }

                    if (userId == null) {
                        return chain.filter(exchange);
                    }

                    // 在你的 filter 中，构造新的请求并移除 Authorization
//                    ServerHttpRequest newRequest = request.mutate()
//                            .headers(h -> h.remove(HttpHeaders.AUTHORIZATION)) // 移除原始 token
//                            .header("X-User-Id", userId.toString())
//                            .build();
//
//                    ServerWebExchange newExchange = exchange.mutate()
//                            .request(newRequest)
//                            .build();

                    ServerHttpRequest mutated = exchange.getRequest()
                            .mutate()
                            .header(HEADER_NAME, userId)
                            .build();
                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutated).build();
                    return chain.filter(mutatedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

}
