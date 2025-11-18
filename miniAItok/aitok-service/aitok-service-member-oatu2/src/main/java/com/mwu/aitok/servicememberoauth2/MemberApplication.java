package com.mwu.aitok.servicememberoauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.mwu.aitok")
@EnableDiscoveryClient
@EntityScan({"com.mwu.aitok.servicememberoauth2.entity", "com.mwu.aitok.model.member.domain"})
@EnableJpaRepositories("com.mwu.aitok.servicememberoauth2.repository")
public class MemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}
