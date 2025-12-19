package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDocumentDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 知识库文档表(KnowledgeDocument)表数据库访问层
 *
 * @author roydon
 * @since 2025-06-03 22:03:43
 */
@Repository
public interface KnowledgeDocumentMapper extends JpaRepository<KnowledgeDocumentDO, Long> {


    Page<KnowledgeDocumentDO> findAllByKnowledgeId(Long knowledgeId, Pageable pageable);
}

