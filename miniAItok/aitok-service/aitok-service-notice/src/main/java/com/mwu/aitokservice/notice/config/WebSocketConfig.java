package com.mwu.aitokservice.notice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/*
ServerEndpointExporter 是 Spring 提供的一个组件，用于自动注册所有使用 @ServerEndpoint 注解声明的 WebSocket endpoint。也就是说，
只要在项目中有用 @ServerEndpoint 注解的类，这个 Bean 会自动帮你把它们注册到 WebSocket 容器中，无需手动配置。
 */
@Configuration
public class WebSocketConfig {
    /**
     * 注入一个ServerEndpointExporter,该Bean会自动注册使用@ServerEndpoint注解申明的websocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
