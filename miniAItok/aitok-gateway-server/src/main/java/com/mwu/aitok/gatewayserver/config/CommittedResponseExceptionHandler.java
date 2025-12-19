package com.mwu.aitok.gatewayserver.config;


import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(-2)
public class CommittedResponseExceptionHandler implements WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CommittedResponseExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof UnsupportedOperationException && exchange.getResponse().isCommitted()) {
            log.debug("Suppressed UnsupportedOperationException after response committed for path: {}",
                    exchange.getRequest().getPath());
            return Mono.empty();
        }
        return Mono.error(ex);
    }
}
