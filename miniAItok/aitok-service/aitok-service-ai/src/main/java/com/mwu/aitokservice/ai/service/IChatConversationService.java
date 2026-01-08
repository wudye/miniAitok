package com.mwu.aitokservice.ai.service;


import com.mwu.aitok.model.ai.domain.chat.ChatConversationDO;
import com.mwu.aitok.model.common.dto.PageDTO;
import org.springframework.data.domain.Page;

/**
 * AI 聊天对话表(AiChatConversation)表服务接口
 *
 * @author roydon
 * @since 2025-04-22 10:13:38
 */
public interface IChatConversationService  {

    Page<ChatConversationDO> getList(PageDTO dto);

    ChatConversationDO validateChatConversationExists(Long id);
}
