package com.mwu.feign.client;

import com.mwu.feign.demo.ApiClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Feign client test successful!";

    }

    @Autowired
    private ApiClient apiClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping("/tes2")
    public void test2() throws InterruptedException {
        System.out.println("Start testing circuit breaker...");
        for (int i = 0; i < 10; i++) {
            apiClient.getUser("test", 500);
            status();
            Thread.sleep(500);
        }
        System.out.println("End testing circuit breaker...");
    }

    private void status() {

        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("ApiService");
        CircuitBreaker.Metrics metrics = breaker.getMetrics();
        System.out.println("------------------------");
        log.info("state={},metrics[failureRate={},bufferedCalls={},failedCalls={},successCalls={},maxBufferCalls={},notPermittedCalls={}]"
                , breaker.getState(), metrics.getFailureRate(), metrics.getNumberOfBufferedCalls(), metrics.getNumberOfFailedCalls()
                , metrics.getNumberOfSuccessfulCalls(), metrics.getNumberOfBufferedCalls(), metrics.getNumberOfNotPermittedCalls());

    }


}
