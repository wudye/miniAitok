package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.AiManagerDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AI管理员表(AiManager)表数据库访问层
 *
 * @author roydon
 * @since 2025-05-30 23:39:16
 */
@Repository
public interface ManagerMapper extends JpaRepository<AiManagerDO, Long> {

    Optional<AiManagerDO> findByUserId(Long userId);
}

