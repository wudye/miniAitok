package com.mwu.aitokservice.notice.controller.v1;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitok.model.notice.vo.WebSocketBaseResp;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
// @ServerEndpoint 注解的类由WebSocket容器管理，不是Spring容器
@ServerEndpoint("/websocket/{userId}")
public class WebSocketServer {

    /*
    Session 对象包含什么
Session 是WebSocket连接的核心对象，包含：

连接状态信息
消息发送通道
连接配置参数
用户认证信息
     */
    // 用来保存在线连接数
    private static final Map<Long, Session> sessionPool = new ConcurrentHashMap<>();
    private static ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        WebSocketServer.objectMapper = objectMapper;
    }

    /* more safe

    private static ObjectMapper getObjectMapper() {
    if (objectMapper == null) {
        // 如果Spring注入失败，手动创建相同配置的ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        // 复制JacksonConfig的配置
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        SimpleModule stringTrimModule = new SimpleModule();
        stringTrimModule.addDeserializer(String.class, new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getValueAsString();
                return value == null ? null : value.trim();
            }
        });
        mapper.registerModule(stringTrimModule);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        objectMapper = mapper;
    }
    return objectMapper;
}

// 方法2：使用时获取
public static <T> void sendOneMessage(Long userId, WebSocketBaseResp<T> message) {
    Session session = sessionPool.get(userId);
    if (session != null && session.isOpen()) {
        String jsonString = getObjectMapper().writeValueAsString(message); // 使用安全的获取方式
        session.getAsyncRemote().sendText(jsonString);
    }
}

     */

    /**
     * 链接成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") Long userId) {
        try {
            session.setMaxIdleTimeout(30000L);  // 30秒空闲超时

            sessionPool.put(userId, session);
            log.info("websocket消息: 有新的连接，总数为:" + sessionPool.size());
        } catch (Exception e) {
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam(value = "userId") Long userId) {
        log.info("websocket消息: 收到客户端消息:" + message);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        sessionPool.remove(userId);
        log.info("有一连接关闭，移除userId={}的用户session, 当前在线人数为：{}", userId, sessionPool.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("错误原因:" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 此为单点消息
     */
    @SneakyThrows
    public static <T> void sendOneMessage(Long userId, WebSocketBaseResp<T> message) {
        Session session = sessionPool.get(userId);
        if (session != null && session.isOpen()) {
            String jsonString = objectMapper.writeValueAsString(message);
            log.info("websocket: 单点消息:" + jsonString);
            session.getAsyncRemote().sendText(jsonString);
        }
    }

    /**
     * 连接是否存在
     *
     * @param userId
     * @return boolean
     */
    public static boolean isConnected(Long userId) {

        return sessionPool.containsKey(userId);
    }

    /**
     * 心跳检测
     *
     * @param ping
     * @return
     */
    public static synchronized int sendPing(String ping) {
        if (sessionPool.isEmpty()) {
            return 0;
        }
        AtomicInteger count = new AtomicInteger(0);
        sessionPool.forEach((userId, session) -> {
            count.getAndIncrement();
            try {
                session.getAsyncRemote().sendText(ping);
            } catch (Exception e) {
                sessionPool.remove(userId);
                log.info("客户端心跳检测异常移除: " + userId + "，心跳发送失败，已移除！");

            }
        });
        return sessionPool.size();
    }


}
