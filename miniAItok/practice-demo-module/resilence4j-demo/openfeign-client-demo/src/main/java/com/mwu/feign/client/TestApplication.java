package com.mwu.feign.client;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.mwu.feign.demo")
public class TestApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(TestApplication.class, args);
    }
}
