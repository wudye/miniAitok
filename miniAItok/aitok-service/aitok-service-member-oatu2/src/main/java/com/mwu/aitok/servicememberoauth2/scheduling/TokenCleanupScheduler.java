package com.mwu.aitok.servicememberoauth2.scheduling;

import com.mwu.aitok.servicememberoauth2.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final TokenRepository tokenRepository;
    // 每天 00:00 执行（按 JVM 默认时区），需要在主应用启用 @EnableScheduling
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanExpiredTokens() {
        Instant now = Instant.now();
        int deleted = tokenRepository.deleteByRefreshExpiryBefore(now);
        log.info("Deleted {} expired tokens on {}", deleted, now);
        // 可添加日志记录 deleted 数量
    }
}
