package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeSegmentDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI 知识库分段表(KnowledgeSegment)表数据库访问层
 *
 * @author roydon
 * @since 2025-06-03 22:03:58
 */
@Repository
public interface KnowledgeSegmentMapper extends JpaRepository<KnowledgeSegmentDO, Long> {
    List<KnowledgeSegmentDO> findAllByDocumentId(Long documentId);

    List<KnowledgeSegmentDO> findAllByVectorIdOrderByIdDesc(String vectorId);

    /**
     * 批量更新检索次数
     *
     * @param ids

    default void updateRetrievalCountIncrByIds(List<Long> ids) {
        update(new LambdaUpdateWrapper<KnowledgeSegmentDO>()
                .setSql(" retrieval_count = retrieval_count + 1")
                .in(KnowledgeSegmentDO::getId, ids));
    }
     */
}

