package com.mwu.aitokservice.ai.service;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.ai.domain.chat.ChatMessageDO;
import com.mwu.aitok.model.ai.vo.chat.ChatMessageVO;
import com.mwu.aitokservice.ai.controller.web.chat.ChatbotController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * IChatMessageService
 *
 * @AUTHOR: roydon
 * @DATE: 2025/4/22
 **/
public interface IChatMessageService {

    /**
     * 获得指定对话的消息列表
     */
    List<ChatMessageDO> listByCid(Long conversationId);

    Flux<R<ChatMessageVO>> sendChatMessageStream(ChatbotController.ChatRequest dto, Long userId);
}
