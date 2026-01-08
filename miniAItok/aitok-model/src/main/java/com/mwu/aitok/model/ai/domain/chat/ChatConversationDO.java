package com.mwu.aitok.model.ai.domain.chat;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * AI 聊天对话表(ai_chat_conversation)实体类
 *
 * @author mwu
 * @since 2025-11-14
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_chat_conversation")
public class ChatConversationDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 对话编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 模型编号
     */
    private Long modelId;
    /**
     * 角色编号
     */
    private Long roleId;
    /**
     * 对话标题
     */
    private String title;
    /**
     * 是否置顶[0否1是]
     */
    private String pinned;
    /**
     * 置顶时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "EUROPE/Vienna")
    private LocalDateTime pinnedTime;
    /**
     * 角色设定
     */
    private String systemMessage;
    /**
     * 温度参数
     */
    private Double temperature;
    /**
     * 单条回复的最大 Token 数量
     */
    private Integer maxTokens;
    /**
     * 上下文的最大 Message 数量
     */
    private Integer maxContexts;
    /**
     * 最后一次回复的内容
     */
    private String lastMessage;

}
