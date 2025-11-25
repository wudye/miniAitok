package com.mwu.aitokservice.notice.schedule;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitok.model.notice.vo.WebSocketBaseResp;
import com.mwu.aitokservice.notice.controller.v1.WebSocketServer;
import com.mwu.aitokservice.notice.enums.HeartCheckMsgEnums;
import com.mwu.aitokservice.notice.enums.WebSocketMsgType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class WebSocketTask {

    @Autowired
    private ObjectMapper objectMapper;
    /**
     * 每30秒进行一次websocket心跳检测
     */
//    @Scheduled(fixedRate = 1000 * 30)
    public void wsHeartCheck() {
        int num = 0;
        try {
            String ping = HeartCheckMsgEnums.PING.getInfo();
            WebSocketBaseResp<String> msg = WebSocketBaseResp.build(WebSocketMsgType.HEART_CHECK.getCode(), ping);
            num = WebSocketServer.sendPing(objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("websocket心跳检测结果，共【{}】个连接", num);
        }
    }
}
