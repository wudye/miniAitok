package com.mwu.aitok.model.ai.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI 知识库段落搜索 Request BO
 */
@Data
public class KnowledgeSegmentSearchReqBO {

    /**
     * 知识库编号
     */
    @NotNull(message = "the knowledge ID cannot be null")
    private Long knowledgeId;

    /**
     * 内容
     */
    @NotEmpty(message = "the content cannot be empty")
    private String content;

    /**
     * 最大返回数量
     */
    private Integer topK;

    /**
     * 相似度阈值
     */
    private Double similarityThreshold;

}
