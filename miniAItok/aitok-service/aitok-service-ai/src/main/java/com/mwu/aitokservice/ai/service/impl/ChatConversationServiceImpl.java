package com.mwu.aitokservice.ai.service.impl;


import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitok.model.ai.domain.chat.ChatConversationDO;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitokservice.ai.mapper.ChatConversationMapper;
import com.mwu.aitokservice.ai.service.IChatConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


/**
 * AI 聊天对话表(AiChatConversation)表服务实现类
 *
 * @author roydon
 * @since 2025-04-22 10:13:39
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatConversationServiceImpl implements IChatConversationService {
    private final ChatConversationMapper chatConversationMapper;

    @Override
    public Page<ChatConversationDO> getList(PageDTO dto) {

        Pageable pageable = PageRequest.of(dto.getPageNum() - 1, dto.getPageSize()
                , Sort.by(Sort.Order.desc("createTime")
        ));


        return chatConversationMapper.findAllByUserId(UserContext.getUserId(), pageable);
    }

    @Override
    public ChatConversationDO validateChatConversationExists(Long id) {
        ChatConversationDO chatConversationDO = chatConversationMapper.findById(id).orElse(null);
        if (chatConversationDO == null) {
            throw new RuntimeException("对话不存在");
        }
        return chatConversationDO;
    }
}
