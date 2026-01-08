package com.mwu.aitok.servicememberoauth2;

import com.mwu.aitokstarter.file.config.MinioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.mwu.aitok.servicememberoauth2", "com.mwu.aitokcommon", "com.mwu.aitiokcoomon", "com.mwu.aitokstarter"})
@EnableDiscoveryClient
@EntityScan({"com.mwu.aitok.servicememberoauth2.entity", "com.mwu.aitok.model.member.domain"})
@EnableJpaRepositories("com.mwu.aitok.servicememberoauth2.repository")
@EnableFeignClients(basePackages = "com.mwu.aitolk.feign")
@EnableScheduling
@ConfigurationPropertiesScan("com.mwu.aitokstarter.file")

/*
@SpringBootApplication(scanBasePackages = "com.mwu.aitok")
@EnableDiscoveryClient
@EntityScan({"com.mwu.aitok.servicememberoauth2.entity", "com.mwu.aitok.model.member.domain"})
@EnableJpaRepositories("com.mwu.aitok.servicememberoauth2.repository")
@EnableFeignClients(basePackages = "com.mwu.aitok")
@EnableScheduling
@EnableConfigurationProperties(MinioProperties.class)
@ConfigurationPropertiesScan("com.mwu.aitokstarter.file.config")

 */
public class MemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}

/*
把 Resilience4j 放在 Service 层、用于保护对外/网络依赖（如调用认证服务器、第三方用户信息、短信/邮件服务等），不要在 Controller 层处理。
常用策略：CircuitBreaker（隔离故障）、Retry（短期重试）、Bulkhead（并发隔离）、TimeLimiter（超时）、RateLimiter（限流）。对本地 DB 操作（如生成/保存 token、简单查询）通常不需要。
为关键方法提供 fallback，记录监控/告警（Micrometer + Prometheus）。集群环境关注降级逻辑幂等与限流策略。
下面给出示例实现：在 login 上添加 CircuitBreaker/Retry/Bulkhead，并提供 fallback。


// java
package com.mwu.aitok.servicememberoauth2.service;

import com.mwu.aitok.model.member.dto.LoginUserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;

import java.util.Collections;
import java.util.Map;

@Service
public class MemberServiceImpl implements MemberService {
    private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);

    // 假设有一个远程认证客户端注入（示例）
    private final AuthClient authClient;

    public MemberServiceImpl(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    @CircuitBreaker(name = "authService", fallbackMethod = "loginFallback")
    @Retry(name = "authService")
    @Bulkhead(name = "authService", type = Bulkhead.Type.SEMAPHORE)
    public Map<String, String> login(LoginUserDTO loginUserDTO) throws Exception {
        // 对外调用放在这里（可能抛异常以触发 retry / circuit breaker）
        return authClient.login(loginUserDTO);
    }

    // fallback 签名：原参数 + Throwable
    public Map<String, String> loginFallback(LoginUserDTO loginUserDTO, Throwable t) {
        log.warn("login fallback for user: {}, reason: {}", loginUserDTO.getUsername(), t.toString());
        // 降级策略：返回空、友好错误或从缓存读取等
        return Collections.emptyMap();
    }

    // 其它接口方法按需实现（本地 DB 操作通常不加 Resilience4j）
}

# yaml
resilience4j:
  circuitbreaker:
    instances:
      authService:
        registerHealthIndicator: true
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 20
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 5
  retry:
    instances:
      authService:
        maxAttempts: 3
        waitDuration: 2s
  bulkhead:
    instances:
      authService:
        maxConcurrentCalls: 10
        maxWaitDuration: 0ms







 */