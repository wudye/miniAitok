package com.mwu.aitokservice.ai.controller.web.chat;


import com.mwu.aitiokcoomon.core.compont.SnowFlake;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitok.model.ai.domain.chat.ChatConversationDO;
import com.mwu.aitok.model.ai.domain.chat.ChatMessageDO;
import com.mwu.aitok.model.ai.domain.model.ChatModelDO;
import com.mwu.aitok.model.ai.domain.model.ModelAgentDO;
import com.mwu.aitok.model.ai.dto.model.web.ChatConversationSaveDTO;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitok.model.common.enums.TrueOrFalseEnum;
import com.mwu.aitokservice.ai.mapper.ChatConversationMapper;
import com.mwu.aitokservice.ai.mapper.ChatMessageMapper;
import com.mwu.aitokservice.ai.mapper.ChatModelMapper;
import com.mwu.aitokservice.ai.service.IChatConversationService;
import com.mwu.aitokservice.ai.service.IChatMessageService;
import com.mwu.aitokservice.ai.service.IChatModelService;
import com.mwu.aitokservice.ai.service.IModelRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * AI 聊天对话表(AiChatConversation)表控制层
 *
 * @author roydon
 * @since 2025-04-22 10:13:36
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("v1/chat/conversation")
public class ChatConversationController {

    private final IChatConversationService chatConversationService;
    private final SnowFlake snowFlake;
    private final IModelRoleService modelRoleService;
    private final IChatMessageService chatMessageService;
    private final IChatModelService chatModelService;
    private final ChatModelMapper chatModelMapper;

    private final ChatMessageMapper chatMessageMapper;
    private final ChatConversationMapper chatConversationMapper;
    /**
     * 分页查询
     */
    @PostMapping("/list")
    public PageData<ChatConversationDO> list(@Validated @RequestBody PageDTO dto) {
        return PageData.page(chatConversationService.getList(dto));
    }

    /**
     * 新增对话
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping
    public R<ChatConversationDO> add(@RequestBody ChatConversationSaveDTO dto) {
        ChatConversationDO chatConversationDO = BeanCopyUtils.copyBean(dto, ChatConversationDO.class);
        chatConversationDO.setId(snowFlake.nextId());
        chatConversationDO.setUserId(UserContext.getUserId());
        chatConversationDO.setCreateBy(UserContext.getUser().getUserName());
        LocalDateTime localDateTime = LocalDateTime.now();
        chatConversationDO.setCreateTime(localDateTime);
        chatConversationDO.setUpdateTime(localDateTime);
        chatConversationDO.setTitle("新对话");
        chatConversationDO.setTemperature(0.75);
        chatConversationDO.setMaxTokens(4096);
        chatConversationDO.setMaxContexts(20);
        // 是否添加了角色
        if (chatConversationDO.getRoleId() != null) {
            ModelAgentDO modelRole = modelRoleService.getModelRole(chatConversationDO.getRoleId());
            if (Objects.isNull(modelRole)) {
                throw new RuntimeException("角色不存在");
            }
            chatConversationDO.setTitle(modelRole.getName());
            chatConversationDO.setSystemMessage(modelRole.getSystemMessage());
            // 获取角色关联的模型，填充参数
            ChatModelDO modelDO = chatModelMapper.findByModel(modelRole.getName());
            if (Objects.isNull(modelDO)) {
                throw new RuntimeException("模型不存在");
            }
            chatConversationDO.setModelId(modelDO.getId());
            chatConversationDO.setTemperature(modelDO.getTemperature().doubleValue());
            chatConversationDO.setMaxTokens(modelDO.getMaxTokens());
            chatConversationDO.setMaxContexts(modelDO.getMaxContexts());
            // 填充默认第一条ai回复预设消息
            ChatMessageDO message = new ChatMessageDO().setConversationId(chatConversationDO.getId())
                    .setModel(modelDO.getModel())
                    .setModelId(modelDO.getId())
                    .setUserId(UserContext.getUserId())
                    .setRoleId(modelRole.getId())
                    .setMessageType(MessageType.ASSISTANT.getValue())
                    .setContent(modelRole.getChatPrologue())
                    .setUseContext(TrueOrFalseEnum.FALSE.getCode());

            chatMessageMapper.save(message);

        }
//        chatConversationDO.setTitle(StringUtils.isEmpty(chatConversationDO.getTitle()) ? "新对话" : chatConversationDO.getTitle());
        chatConversationMapper.save(chatConversationDO);

        return R.ok(chatConversationDO);
    }

    /**
     * 编辑数据
     */
    @PutMapping
    public R<?> edit(@RequestBody ChatConversationDO chatConversationDO) {
        chatConversationDO.setUpdateBy(UserContext.getUser().getUserName());
        chatConversationDO.setUpdateTime(LocalDateTime.now());
        return R.ok(chatConversationMapper.save(chatConversationDO));
    }

    /**
     * 删除数据
     */
    @DeleteMapping("/{id}")
    public R<?> removeById(@PathVariable Long id) {
        chatConversationMapper.deleteById(id);
        return R.ok("删除成功");
    }

}

