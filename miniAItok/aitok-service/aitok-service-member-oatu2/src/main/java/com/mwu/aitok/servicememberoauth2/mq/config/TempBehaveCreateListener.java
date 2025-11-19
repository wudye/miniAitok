package com.mwu.aitok.servicememberoauth2.mq.config;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.mwu.aitok.model.behave.mq.BehaveQueueConstant.BEHAVE_EXCHANGE;
import static com.mwu.aitok.model.behave.mq.BehaveQueueConstant.FAVORITE_DIRECT_QUEUE;

@Component
public class TempBehaveCreateListener {

    // 假设队列名为 behave.create.queue，需与绑定配置一致
    @RabbitListener(queues = FAVORITE_DIRECT_QUEUE)
    public void handleCreate(String userId) {
        // 处理接收到的消息（这里是用户 ID 字符串）
        System.out.println("Received userId: " + userId);
        // 业务处理逻辑...
    }
}

