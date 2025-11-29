package com.mwu.aitokservice.ai.service.impl;


import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.utils.bean.BeanUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.model.ai.bo.KnowledgeSegmentSearchReqBO;
import com.mwu.aitok.model.ai.bo.KnowledgeSegmentSearchRespBO;
import com.mwu.aitok.model.ai.domain.chat.ChatConversationDO;
import com.mwu.aitok.model.ai.domain.chat.ChatMessageDO;
import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDO;
import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDocumentDO;
import com.mwu.aitok.model.ai.domain.model.ChatModelDO;
import com.mwu.aitok.model.ai.domain.model.ModelAgentDO;
import com.mwu.aitok.model.ai.domain.model.ToolDO;
import com.mwu.aitok.model.ai.vo.chat.ChatMessageRespVO;
import com.mwu.aitok.model.ai.vo.chat.ChatMessageVO;
import com.mwu.aitokcommon.ai.enums.AiPlatformEnum;
import com.mwu.aitokcommon.ai.util.AiUtils;
import com.mwu.aitokservice.ai.controller.web.chat.ChatbotController;
import com.mwu.aitokservice.ai.mapper.ChatConversationMapper;
import com.mwu.aitokservice.ai.mapper.ChatMessageMapper;
import com.mwu.aitokservice.ai.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.mwu.aitiokcoomon.core.utils.CustomCollectionUtils.convertList;
import static com.mwu.aitiokcoomon.core.utils.CustomCollectionUtils.convertSet;


/**
 * ChatMessageServiceImpl
 *
 * @AUTHOR: roydon
 * @DATE: 2025/4/22
 **/
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatMessageServiceImpl  implements IChatMessageService {

    /**
     * 知识库转 {@link UserMessage} 的内容模版
     */
    private static final String KNOWLEDGE_USER_MESSAGE_TEMPLATE = "使用 <Reference></Reference> 标记中的内容作为本次对话的参考:\n\n" +
            "%s\n\n" + // 多个 <Reference></Reference> 的拼接
            "回答要求：\n- 避免提及你是从 <Reference></Reference> 获取的知识。";

    private final ChatMessageMapper chatMessageMapper;
    private final IChatConversationService chatConversationService;
    private final IChatModelService chatModelService;
    private final IModelRoleService modelRoleService;
    private final IKnowledgeSegmentService knowledgeSegmentService;
    private final IKnowledgeDocumentService knowledgeDocumentService;
    private final IToolService toolService;
    private final ChatConversationMapper chatConversationMapper;
    /**
     * 获得指定对话的消息列表
     */
    @Override
    public List<ChatMessageDO> listByCid(Long conversationId) {
        ChatConversationDO conversationDO = chatConversationMapper.findById(conversationId).orElse(null);
        if (Objects.isNull(conversationDO) || !Objects.equals(UserContext.getUserId(), conversationDO.getUserId())) {
            return null;
        }

        return chatMessageMapper.findByConversationIdOrderByIdAsc(conversationId);
    }

    @Override
    public Flux<R<ChatMessageVO>> sendChatMessageStream(ChatbotController.ChatRequest dto, Long userId) {
        // 1.1 校验对话存在
        ChatConversationDO conversation = chatConversationService.validateChatConversationExists(dto.conversationId());
        if (Objects.isNull(conversation) || Objects.equals(conversation.getUserId(), userId)) {
            throw new RuntimeException("对话不存在");
        }
        List<ChatMessageDO> contextMessages = this.selectListByConversation(conversation.getId(), conversation.getMaxContexts());
        // 1.2 校验模型
        ChatModelDO model = chatModelService.validateModel(conversation.getModelId());
        StreamingChatModel chatModel = chatModelService.getChatModel(model.getId());

        // 2. 知识库召回
        List<KnowledgeSegmentSearchRespBO> knowledgeSegments = recallKnowledgeSegment(dto.message(), conversation);

        // 3. 插入 user 发送消息
        ChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model, userId, conversation.getRoleId(), MessageType.USER, dto.message(), dto.useContext(), null);

        // 4.1 插入 assistant 接收消息
        ChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model, userId, conversation.getRoleId(), MessageType.ASSISTANT, "", dto.useContext(), knowledgeSegments);

        // 4.2 构建 Prompt，并进行调用
        Prompt prompt = buildPrompt(userId, conversation, contextMessages, knowledgeSegments, model, dto);
        Flux<ChatResponse> streamResponse = chatModel.stream(prompt);

        // 4.3 流式返回
        StringBuffer contentBuffer = new StringBuffer();
        return streamResponse.map(chunk -> {
            // 处理知识库的返回，只有首次才有
            List<ChatMessageRespVO.KnowledgeSegment> segments = null;
            if (StringUtils.isEmpty(contentBuffer)) {
                Map<Long, KnowledgeDocumentDO> documentMap = knowledgeDocumentService.getKnowledgeDocumentMap(convertSet(knowledgeSegments, KnowledgeSegmentSearchRespBO::getDocumentId));
                segments = BeanUtils.toBean(knowledgeSegments, ChatMessageRespVO.KnowledgeSegment.class, segment -> {
                    KnowledgeDocumentDO document = documentMap.get(segment.getDocumentId());
                    segment.setDocumentName(document != null ? document.getName() : null);
                });
            }
            // 响应结果
            String newContent = chunk.getResult() != null ? chunk.getResult().getOutput().getText() : null;
            newContent = org.apache.commons.lang3.StringUtils.defaultIfBlank(newContent, ""); // 避免 null 的 情况
            contentBuffer.append(newContent);
            return R.ok(new ChatMessageVO()
                    .setSend(BeanUtils.toBean(userMessage, ChatMessageVO.Message.class))
                    .setReceive(BeanUtils.toBean(assistantMessage, ChatMessageVO.Message.class).setContent(newContent).setSegments(segments)));
        }).publishOn(Schedulers.boundedElastic()).doOnComplete(() -> {
            // 更新assistant的内容
            ChatMessageDO chatMessageDO = new ChatMessageDO().setId(assistantMessage.getId()).setContent(contentBuffer.toString());
            chatMessageMapper.save(chatMessageDO);
            // 更新对话的最后一次回复时间与最新回复内容
            ChatConversationDO chatConversationDO =new ChatConversationDO().setId(conversation.getId()).setLastMessage(contentBuffer.substring(0, Math.min(contentBuffer.length(), 64)));
            chatConversationMapper.save(chatConversationDO) ;
        }).doOnError(throwable -> {
            log.error("[sendChatMessageStream][userId({}) dto({}) 发生异常]", userId, dto, throwable);
            ChatMessageDO chatMessageDO = new ChatMessageDO().setId(assistantMessage.getId()).setContent(throwable.getMessage());
            chatMessageMapper.save(chatMessageDO);
            // 更新对话的最后一次回复时间与最新回复内容
            ChatConversationDO chatConversationDO =  new ChatConversationDO().setId(conversation.getId()).setLastMessage(throwable.getMessage());
            chatConversationMapper.save(chatConversationDO);
        }).onErrorResume(error -> Flux.just(R.fail("对话生成异常")));
    }

    /**
     * 获得指定对话的消息列表
     */
    List<ChatMessageDO> selectListByConversation(Long conversationId, Integer maxContexts) {
        int limit = (maxContexts == null || maxContexts <= 0) ? 10 : Math.max(1, maxContexts * 2);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return chatMessageMapper.findByConversationId(conversationId, pageable);
    }

    /**
     * 知识库召回
     */
    private List<KnowledgeSegmentSearchRespBO> recallKnowledgeSegment(String content, ChatConversationDO conversation) {
        // 1. 查询智能体
        if (conversation == null || conversation.getRoleId() == null) {
            return Collections.emptyList();
        }
        ModelAgentDO role = modelRoleService.getModelRole(conversation.getRoleId());
        if (role == null || CollectionUtils.isEmpty(role.getKnowledgeIds())) {
            return Collections.emptyList();
        }

        // 2. 遍历召回
        // TODO redesign ChatMessageDO
        List<KnowledgeSegmentSearchRespBO> knowledgeSegments = new ArrayList<>();
        for (KnowledgeDO knowledgeId : role.getKnowledgeIds()) {
            //knowledgeSegments.addAll(knowledgeSegmentService.searchKnowledgeSegment(new KnowledgeSegmentSearchReqBO().setKnowledgeId(knowledgeId).setContent(content)));
        }
        return knowledgeSegments;
    }

    /**
     * 创建消息
     */
    private ChatMessageDO createChatMessage(Long conversationId, Long replyId,
                                            ChatModelDO model, Long userId, Long roleId,
                                            MessageType messageType, String content, Boolean useContext,
                                            List<KnowledgeSegmentSearchRespBO> knowledgeSegments) {
        ChatMessageDO message = new ChatMessageDO().setConversationId(conversationId).setReplyId(replyId)
                .setModel(model.getModel()).setModelId(model.getId()).setUserId(userId).setRoleId(roleId)
                .setMessageType(messageType.getValue()).setContent(content).setUseContext(useContext ? "1" : "0");
           //     .setSegmentIds(convertList(knowledgeSegments, KnowledgeSegmentSearchRespBO::getId));
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.save(message);
        return message;
    }

    /**
     * 构建 Prompt
     */
    private Prompt buildPrompt(Long userId, ChatConversationDO conversation, List<ChatMessageDO> contextMessages,
                               List<KnowledgeSegmentSearchRespBO> knowledgeSegments,
                               ChatModelDO model, ChatbotController.ChatRequest dto) {
        List<Message> chatMessages = new ArrayList<>();
        // 1.1 System Context 角色设定
        if (StringUtils.isNotBlank(conversation.getSystemMessage())) {
            chatMessages.add(new SystemMessage(conversation.getSystemMessage()));
        }

        // 1.2 context message 历史消息
        contextMessages.forEach(message -> chatMessages.add(AiUtils.buildMessage(message.getMessageType(), message.getContent())));

        // 1.3 当前 user message 新发送消息
        chatMessages.add(new UserMessage(dto.message()));

        // 1.4 知识库，通过 UserMessage 实现
        if (CollectionUtils.isNotEmpty(knowledgeSegments)) {
            String reference = knowledgeSegments.stream()
                    .map(segment -> "<Reference>" + segment.getContent() + "</Reference>")
                    .collect(Collectors.joining("\n\n"));
            chatMessages.add(new UserMessage(String.format(KNOWLEDGE_USER_MESSAGE_TEMPLATE, reference)));
        }

        // 2.1 查询 tool 工具
        Set<String> toolNames = null;
        Map<String, Object> toolContext = Map.of();
        if (conversation.getRoleId() != null) {
            ModelAgentDO chatRole = modelRoleService.getModelRole(conversation.getRoleId());
            if (chatRole != null && CollectionUtils.isNotEmpty(chatRole.getToolIds())) {
                List<ToolDO> tools = chatRole.getToolIds();
                List<Long> toolIds = tools.stream().map(ToolDO::getId).toList();

                toolNames = convertSet(toolService.getToolList(toolIds), ToolDO::getName);
                // todo @roydon 工具集合
                toolContext = AiUtils.buildCommonToolContext(userId);
            }
        }
        // 2.2 构建 ChatOptions 对象
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions chatOptions = AiUtils.buildChatOptions(platform, model.getModel(), conversation.getTemperature(), conversation.getMaxTokens(), toolNames, toolContext);
        return new Prompt(chatMessages, chatOptions);
    }


}
