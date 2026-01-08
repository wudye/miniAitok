package com.mwu.aitok.servicememberoauth2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("member-oauth2 Service API Documentation")
                        .description("API documentation for the member-oauth2 service")
                        .version("1.0.0"));
    }
}
