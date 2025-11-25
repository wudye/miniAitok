package com.mwu.aitok.service.video.mq.listener;

import com.mwu.aitok.service.video.service.IVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.mwu.aitok.model.video.mq.VideoDirectExchangeConstant.DIRECT_KEY_INFO;
import static com.mwu.aitok.model.video.mq.VideoDirectExchangeConstant.EXCHANGE_VIDEO_DIRECT;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoRabbitListener {

    private final IVideoService videoService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = DIRECT_KEY_INFO),
            exchange= @Exchange(name = EXCHANGE_VIDEO_DIRECT, type = ExchangeTypes.DIRECT),
            key = DIRECT_KEY_INFO

    ))
    public void listenVideoInfoMessage(String msg) {
        // 接收到的为videoId
        log.info("video 接收到获取视频详情消息：{}", msg);
        // 同步video info到db
        videoService.updateVideoInfo(msg);
    }
}
