package com.mwu.aitok.model.ai.domain.knowledge;


import com.mwu.aitok.model.ai.domain.chat.ChatMessageDO;
import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;

/**
 * AI 知识库分段表(KnowledgeSegment)实体类
 *
 * @author mwu
 * @since 2025-06-03 22:03:58
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_knowledge_segment")
public class KnowledgeSegmentDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = -80429395507510721L;

    /**
     * 向量库的编号 - 空值
     */
    public static final String VECTOR_ID_EMPTY = "";

    /**
     * 编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 知识库编号
     */
    private Long knowledgeId;
    /**
     * 文档编号
     */
    private Long documentId;
    /**
     * 分段内容
     */
    private String content;
    /**
     * 字符数
     */
    private Integer contentLength;
    /**
     * 向量库的编号
     */
    private String vectorId;
    /**
     * token 数量
     */
    private Integer tokens;
    /**
     * 召回次数
     */
    private Integer retrievalCount;
    /**
     * 是否启用
     */
    private String stateFlag;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "message_id")
    private ChatMessageDO chatMessageDO;


}

