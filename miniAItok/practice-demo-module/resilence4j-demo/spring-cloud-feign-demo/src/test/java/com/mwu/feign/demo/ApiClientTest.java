// java
package com.mwu.feign.demo;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = ApiClientTest.TestConfig.class)
@Slf4j
public class ApiClientTest {

    @MockitoBean
    private ApiClient apiClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Test
    void test() throws Exception {
        for (int i = 0; i < 10; i++) {
            apiClient.getUser("test", 500);
            status();
            Thread.sleep(500);
        }
    }

    private void status() {

        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("ApiService");
        CircuitBreaker.Metrics metrics = breaker.getMetrics();
        System.out.println("------------------------");
        log.info("state={},metrics[failureRate={},bufferedCalls={},failedCalls={},successCalls={},maxBufferCalls={},notPermittedCalls={}]"
                , breaker.getState(), metrics.getFailureRate(), metrics.getNumberOfBufferedCalls(), metrics.getNumberOfFailedCalls()
                , metrics.getNumberOfSuccessfulCalls(), metrics.getNumberOfBufferedCalls(), metrics.getNumberOfNotPermittedCalls());

    }

    @Configuration
    static class TestConfig {
        @Bean
        public CircuitBreakerRegistry circuitBreakerRegistry() {
            return CircuitBreakerRegistry.ofDefaults();
        }
    }
}
