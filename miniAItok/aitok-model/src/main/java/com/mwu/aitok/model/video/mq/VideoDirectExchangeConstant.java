package com.mwu.aitok.model.video.mq;

/**
 * video模块路由交换机配置
 *
 * @AUTHOR: mwu
 * @DATE: 2024/2/4
 **/
public class VideoDirectExchangeConstant {

    // 交换机
    public static final String EXCHANGE_VIDEO_DIRECT = "video.direct";
    // 队列
    public static final String DIRECT_QUEUE_INFO = "video.queue.info";
    // 绑定的routing key
    public static final String DIRECT_KEY_INFO = "info";

}
