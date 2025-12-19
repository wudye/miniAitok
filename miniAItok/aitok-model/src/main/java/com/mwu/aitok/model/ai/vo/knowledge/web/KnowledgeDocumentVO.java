package com.mwu.aitok.model.ai.vo.knowledge.web;

import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDocumentDO;
import lombok.Data;

@Data
public class KnowledgeDocumentVO extends KnowledgeDocumentDO {

    // 向量化状态
    private String embeddingState;

}

