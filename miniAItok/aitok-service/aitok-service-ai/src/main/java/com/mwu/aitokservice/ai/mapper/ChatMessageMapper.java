package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.domain.chat.ChatMessageDO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ChatMessageMapper
 *
 * @AUTHOR: roydon
 * @DATE: 2025/4/22
 **/
@Repository
public interface ChatMessageMapper extends JpaRepository<ChatMessageDO, Long> {

    List<ChatMessageDO> findByConversationIdOrderByIdAsc(Long conversationId);

    List<ChatMessageDO> findByConversationId(Long conversationId, Pageable pageable);
}
