package com.mwu.feign.demo;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="ApiService", url="http://localhost:8082")
public interface ApiClient {

    @GetMapping("/api/test")
    @CircuitBreaker(name = "ApiService", fallbackMethod = "fallback")
    String getUser(@RequestParam(name="param") String param, @RequestParam(name="time") int time);

    default String fallback(String param, int time, Throwable throwable) {
        return "fallback";
    }
}
