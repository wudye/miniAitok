package com.mwu.aitokservice.search.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.mwu.aitok.model.video.mq.VideoDelayedQueueConstant.*;

/**
 * RabbitMQ搜索服务配置
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/31
 */
@Configuration
public class RabbitMqSearchConfig {

    /**
     * ES同步队列
     */
    @Bean
    public Queue esSyncQueue() {
        return new Queue(ESSYNC_DIRECT_QUEUE, true);
    }

    /**
     * ES同步交换器（临时使用普通direct交换器）
     */
    @Bean
    public DirectExchange esSyncExchange() {
        return new DirectExchange(ESSYNC_DELAYED_EXCHANGE, true, false);
    }

    /**
     * 绑定队列到交换器
     */
    @Bean
    public Binding esSyncBinding() {
        return BindingBuilder.bind(esSyncQueue())
                .to(esSyncExchange())
                .with(ESSYNC_ROUTING_KEY);
    }
}