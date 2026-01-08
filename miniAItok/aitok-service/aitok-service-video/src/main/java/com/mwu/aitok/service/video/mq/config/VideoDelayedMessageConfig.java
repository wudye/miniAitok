package com.mwu.aitok.service.video.mq.config;



import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.HashMap;
import java.util.Map;

import static com.mwu.aitok.model.video.mq.VideoDelayedQueueConstant.*;
import static com.mwu.aitok.model.video.mq.VideoDirectExchangeConstant.*;

@Configuration
public class VideoDelayedMessageConfig {



    @Bean
    public Queue infoQueue() {
        return new Queue(DIRECT_KEY_INFO, true);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }
    // 延迟交换机配置暂时注释，需要安装 RabbitMQ 延迟消息插件
    // 临时解决方案：创建普通队列，使用 TTL + DLX 实现延迟消息
    @Bean
    public Queue esSyncQueue() {
        return new Queue(ESSYNC_DIRECT_QUEUE,true);
    }

    /*
    @Bean
    public CustomExchange orderDelayedExchange() {
        Map<String,Object> map= new HashMap<>();
        map.put("x-delayed-type","direct");
        return new CustomExchange(ESSYNC_DELAYED_EXCHANGE,"x-delayed-message",true,false,map);
    }

    @Bean
    public Binding delayOrderBinding() {
        return BindingBuilder.bind(esSyncQueue()).to(orderDelayedExchange()).with(ESSYNC_ROUTING_KEY).noargs();
    }
    */


}
