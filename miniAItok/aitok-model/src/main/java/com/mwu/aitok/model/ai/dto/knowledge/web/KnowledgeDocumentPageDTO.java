package com.mwu.aitok.model.ai.dto.knowledge.web;


import com.mwu.aitok.model.search.dto.PageDTO;
import lombok.Data;

/**
 * KnowledgeDocumentPageDTO
 *
 * @AUTHOR: mwu
 * @DATE: 2025/7/10
 **/
@Data
public class KnowledgeDocumentPageDTO extends PageDTO {
    /**
     * 知识库id
     */
    private Long knowledgeId;
}
