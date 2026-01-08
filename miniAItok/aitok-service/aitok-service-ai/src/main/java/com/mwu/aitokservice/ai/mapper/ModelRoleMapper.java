package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.domain.model.ModelAgentDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * AI 智能体表(ModelRole)表数据库访问层
 *
 * @author roydon
 * @since 2025-06-02 15:30:42
 */
@Repository
public interface ModelRoleMapper extends JpaRepository<ModelAgentDO, Long>, JpaSpecificationExecutor<ModelAgentDO> {

}
