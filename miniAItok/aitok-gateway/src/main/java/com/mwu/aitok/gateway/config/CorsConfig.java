package com.mwu.aitok.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * cors config
 * @author mwu
 * @since 2025-11-13
 */
//@Configuration
public class CorsConfig {

    /*
    三处配置 CORS：Tomcat（容器层）、Spring Security（安全过滤链）和 Spring MVC / WebFlux（框架层）
    Tomcat（容器层）：全局生效、覆盖整个容器内所有应用，灵活性和与 Spring 集成性较差；适合在独立容器或中间件级统一策略时使用。
Spring Security（安全层）：如果应用启用了 Spring Security，必须在安全链里允许 CORS，否则预检（OPTIONS）或被安全过滤器拦截。推荐在有安全的系统中以此为准（并把具体 CORS 配置作为来源注入给 Security）。
Spring MVC / WebFlux（框架层） 适用于无 Security 或 Security 已允许 CORS 的情形
：针对控制器路由配置，灵活，易于按路径或控制器细化。WebMVC 用 WebMvcConfigurer，WebFlux 用 CorsWebFilter。

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("*"); // 允许所有的方法
        config.addAllowedOrigin("*"); // 运行所有的域进行请求
        config.addAllowedHeader("*"); // 允许所有的请求头
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config); // 针对所有的请求都支持跨域
        return new CorsWebFilter(source);
    }


     */

}