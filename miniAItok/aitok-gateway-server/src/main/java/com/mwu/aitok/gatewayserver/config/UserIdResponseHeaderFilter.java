//package com.mwu.aitok.gatewayserver.config;
//
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.ServerWebExchangeDecorator;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//
//@Component
//@Slf4j
//public class UserIdResponseHeaderFilter implements WebFilter {
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        // 使用装饰器来修改响应头 ReadOnlyHttpHeaders异常 - 响应头是只读的，无法直接添加
//        ServerWebExchangeDecorator decoratedExchange = new ServerWebExchangeDecorator(exchange) {
//            @Override
//            public ServerHttpResponse getResponse() {
//                return new ServerHttpResponseDecorator(exchange.getResponse()) {
//                    @Override
//                    public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends org.springframework.core.io.buffer.DataBuffer> body) {
//                        // 在写入响应前添加用户ID头
//                        addUserIdHeader();
//                        return super.writeWith(body);
//                    }
//
//                    private void addUserIdHeader() {
//                        try {
//                            HttpHeaders headers = getDelegate().getHeaders();
//
//                            // 避免重复添加
//                            if (headers.containsKey("X-User-Id")) {
//                                return;
//                            }
//
//                            // 从安全上下文获取用户信息
//                            ReactiveSecurityContextHolder.getContext()
//                                    .map(context -> {
//                                        Authentication auth = context.getAuthentication();
//                                        return extractUserId(auth);
//                                    })
//                                    .defaultIfEmpty("anonymous")
//                                    .subscribe(userId -> {
//                                        if (userId != null && !headers.containsKey("X-User-Id")) {
//                                            headers.add("X-User-Id", userId);
//                                            log.debug("Added X-User-Id header: {}", userId);
//                                        }
//                                    });
//                        } catch (Exception e) {
//                            log.error("Error adding user ID header", e);
//                        }
//                    }
//                };
//            }
//        };
//
//        return chain.filter(decoratedExchange);
//    }
//
//    private String extractUserId(Authentication auth) {
//        if (auth == null || !auth.isAuthenticated()) {
//            return "anonymous";
//        }
//
//        try {
//            if (auth instanceof JwtAuthenticationToken) {
//                Jwt jwt = ((JwtAuthenticationToken) auth).getToken();
//                String userId = jwt.getClaim("userid");
//
//
//                return userId != null ? userId : "anonymous";
//            }
//        } catch (Exception e) {
//            log.error("Error extracting user ID from authentication", e);
//        }
//
//        return "anonymous";
//    }
//}
