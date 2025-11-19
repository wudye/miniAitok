package com.mwu.aitok.model.behave.mq;

/**
 * BehaveQueueConstant
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/31
 * 视频延时队列常量配置
 **/
public class BehaveQueueConstant {
    /**
     * 交换机
     * 交换机是消息队列系统中的核心组件，用于接收消息并根据路由规则将消息分发到绑定的队列中。
     * 这里的交换机名称为 "exchange.behave.favorite"，表明它可能与“行为（Behave）”相关的功能模块有关。
     */
    public static final String BEHAVE_EXCHANGE = "exchange.behave.favorite";
    /**
     * 队列
     * 队列是消息的存储容器，消费者从队列中获取消息进行处理。
     * 这里的队列名称为 "queue.favorite.create"，可能用于处理“收藏夹创建”相关的消息。
     */
    public static final String FAVORITE_DIRECT_QUEUE = "queue.favorite.create";
    /**
     * 绑定的routing key
     * 路由键是消息从交换机路由到队列的依据。这里的路由键为 "favorite.create"，表明该键可能用于标识“创建收藏夹”操作的消息。
     */
    public static final String CREATE_ROUTING_KEY = "favorite.create";

}