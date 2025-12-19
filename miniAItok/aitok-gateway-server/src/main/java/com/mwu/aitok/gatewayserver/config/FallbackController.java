package com.mwu.aitok.gatewayserver.config;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@RestController
public class FallbackController {

    @GetMapping("/fallback/account")
    public ResponseEntity<String> accountFallback() {
        return ResponseEntity.status(503).body("account service unavailable - fallback");
    }
    @GetMapping("/fallback/default")
    public Mono<Void> defaultFallback(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.empty();
        }

        byte[] bytes = "default for all service unavailable - fallback".getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.TEXT_PLAIN);

        // 在 commit 前注册，保证 headers 在真正 commit 之前就已设置
        response.beforeCommit(() -> {
            response.getHeaders().setContentLength(bytes.length);
            return Mono.empty();
        });

        return response.writeWith(Mono.just(buffer));
    }
}