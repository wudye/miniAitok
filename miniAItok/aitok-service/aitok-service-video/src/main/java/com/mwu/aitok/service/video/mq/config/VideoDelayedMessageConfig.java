package com.mwu.aitok.service.video.mq.config;



import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.HashMap;
import java.util.Map;

import static com.mwu.aitok.model.video.mq.VideoDelayedQueueConstant.*;

@Configuration
public class VideoDelayedMessageConfig {

    @Bean
    public Queue esSyncQueue() {
        return new Queue(ESSYNC_DIRECT_QUEUE,true);
    }

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
}
