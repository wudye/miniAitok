package com.mwu.aitok.servicememberoauth2;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static com.mwu.aitok.model.behave.mq.BehaveQueueConstant.BEHAVE_EXCHANGE;
import static com.mwu.aitok.model.behave.mq.BehaveQueueConstant.CREATE_ROUTING_KEY;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class MqTest {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage() {
        rabbitTemplate.convertAndSend("behave.create.queue", "123");
        System.out.println("Message sent to behave.create.queue");
    }

    @Test
    void testPublishBehaveFavoriteCreate() {


        rabbitTemplate.convertAndSend(BEHAVE_EXCHANGE, CREATE_ROUTING_KEY, "1");
        System.out.println("Message sent to behave.create.queue");

    }
}
