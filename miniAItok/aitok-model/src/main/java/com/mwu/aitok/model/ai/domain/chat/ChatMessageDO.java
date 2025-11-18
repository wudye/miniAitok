package com.mwu.aitok.model.ai.domain.chat;


import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeSegmentDO;
import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 聊天消息表(ai_chat_message)实体类
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
@Table(name = "ai_chat_message")
public class ChatMessageDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = 262502003074283660L;
    /**
     * 消息编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 对话编号
     */
    private Long conversationId;
    /**
     * 回复编号
     */
    private Long replyId;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 对话类型
     */
    private String messageType;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 是否携带上下文[0否1是]
     */
    private String useContext;

    /**
     * roleId c
     *
     * 关联 {@link ModelAgentDO#getId()} 字段
     */
    private Long roleId;

    /**
     * 模型标志
     *
     * 冗余 {@link ChatModelDO#getModel()}
     */
    private String model;
    /**
     * 模型编号
     *
     * 关联 {@link ChatModelDO#getId()} 字段
     */
    private Long modelId;
    /**
     * 知识库段落编号数组
     *
     * 关联 {@link KnowledgeSegmentDO#getId()} 字段
     */
  //  @TableField(typeHandler = LongListTypeHandler.class)
    @OneToMany(mappedBy = "chatMessageDO", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KnowledgeSegmentDO> segmentDOS = new ArrayList<>();

}
