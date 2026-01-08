package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.domain.model.ModelAgentCategoryDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI 智能体分类表(AiModelAgentCategory)表数据库访问层
 *
 * @author roydon
 * @since 2025-06-13 10:21:49
 */
@Repository
public interface ModelAgentCategoryMapper extends JpaRepository<ModelAgentCategoryDO, Long> {


    Page<ModelAgentCategoryDO> findAllByName(String name, Pageable pageable);
}

