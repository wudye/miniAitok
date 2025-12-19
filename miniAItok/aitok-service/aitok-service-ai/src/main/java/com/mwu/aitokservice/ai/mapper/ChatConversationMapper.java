package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.domain.chat.ChatConversationDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 聊天对话表(AiChatConversation)表数据库访问层
 *
 * @author roydon
 * @since 2025-04-22 10:13:36
 */
@Repository
public interface ChatConversationMapper extends JpaRepository<ChatConversationDO, Long> {


    Page<ChatConversationDO> findAllByUserId(Long userId, Pageable pageable);
}

