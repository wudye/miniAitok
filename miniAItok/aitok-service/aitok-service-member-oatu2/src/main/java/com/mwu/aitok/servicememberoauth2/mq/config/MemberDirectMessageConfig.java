package com.mwu.aitok.servicememberoauth2.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.mwu.aitok.model.behave.mq.BehaveQueueConstant.*;

/**
 * MemberDirectMessageConfig
 *
 * @AUTHOR: roydon
 * @DATE: 2023/10/11
 * 延时交换机绑定
 **/
@Configuration
public class MemberDirectMessageConfig {

    /*
    这里使用了 Queue 类来创建一个持久化队列，队列名称由常量 FAVORITE_DIRECT_QUEUE 指定。持久化队列在 RabbitMQ 重启后依然存在，适合需要可靠消息传递的场景。
     */
    @Bean
    public Queue favoriteDirectQueue() {
        return new Queue(FAVORITE_DIRECT_QUEUE, true);
    }

    /**
     *
     * 交换机的类型为 x-delayed-message
     * 该方法创建了一个 DirectExchange 类型的交换机，名称由常量 BEHAVE_EXCHANGE 指定。DirectExchange 是一种直连交换机，消息会根据路由键精确匹配到绑定的队列。
     */
    @Bean
    public Exchange behaveExchange() {
        return new DirectExchange(BEHAVE_EXCHANGE);
    }

    /*
    这里通过 BindingBuilder 将队列绑定到交换机，并指定路由键 CREATE_ROUTING_KEY。路由键用于匹配消息，使得消息能够正确路由到目标队列。
     */
    @Bean
    public Binding behaveFavoriteCreateBinding() {
        return BindingBuilder.bind(favoriteDirectQueue()).to(behaveExchange()).with(CREATE_ROUTING_KEY).noargs();


    }

}
