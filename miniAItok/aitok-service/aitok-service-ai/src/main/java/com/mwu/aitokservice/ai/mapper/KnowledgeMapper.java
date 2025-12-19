package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI 知识库表(Knowledge)表数据库访问层
 *
 * @author roydon
 * @since 2025-06-03 22:03:24
 */
@Repository
public interface KnowledgeMapper extends JpaRepository<KnowledgeDO, Long> {


    List<KnowledgeDO> findByUserIdAndStateFlag(Long userId, String stateFlag);

    Page<KnowledgeDO> findAllByUserId(Long userId, Pageable pageable);
}

