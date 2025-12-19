package com.mwu.aitokservice.ai.service;


import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDO;
import com.mwu.aitok.model.common.dto.PageDTO;

/**
 * AI 知识库表(Knowledge)表服务接口
 *
 * @author roydon
 * @since 2025-06-03 22:03:27
 */
public interface IKnowledgeService {

    KnowledgeDO validateKnowledgeExists(Long knowledgeId);

    /**
     * 创建知识库
     */
    Long createKnowledge(KnowledgeDO dto);

    Long editKnowledge(KnowledgeDO dto);

    /**
     * 获取知识库分页列表
     */
    PageData<KnowledgeDO> knowledgeList(PageDTO pageDTO);
}
