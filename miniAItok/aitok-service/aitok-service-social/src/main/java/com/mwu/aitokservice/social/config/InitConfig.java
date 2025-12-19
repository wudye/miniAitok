package com.mwu.aitokservice.social.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.mwu.aitok.feign.member.fallback")
public class InitConfig {
}
