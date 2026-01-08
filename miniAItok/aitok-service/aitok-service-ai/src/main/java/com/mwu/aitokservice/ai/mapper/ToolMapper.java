package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.domain.model.ToolDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI 工具表(Tool)表数据库访问层
 *
 * @author roydon
 * @since 2025-06-05 16:02:46
 */
@Repository
public interface ToolMapper extends JpaRepository<ToolDO, Long>, JpaSpecificationExecutor<ToolDO> {

    List<ToolDO> findByStateFlag(String stateFlag);
}

