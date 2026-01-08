package com.mwu.aitok.model.ai.domain.knowledge;


import com.mwu.aitok.model.common.BaseDO;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;

/**
 * AI 知识库文档表(KnowledgeDocument)实体类
 *
 * @author mwu
 * @since 2025-06-03 22:03:43
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ai_knowledge_document")
public class KnowledgeDocumentDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = -53438125161664572L;
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
     * 文档名称
     */
    private String name;
    /**
     * 文件 URL
     */
    private String url;
    /**
     * 内容
     */
    private String content;
    /**
     * 字符数
     */
    private Integer contentLength;
    /**
     * token 数量
     */
    private Integer tokens;
    /**
     * 分片最大 Token 数
     */
    private Integer segmentMaxTokens;
    /**
     * 召回次数
     */
    private Integer retrievalCount;
    /**
     * 是否启用
     */
    private String stateFlag;


}

