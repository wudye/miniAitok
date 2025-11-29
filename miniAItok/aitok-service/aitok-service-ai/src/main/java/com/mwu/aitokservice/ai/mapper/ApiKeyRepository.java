package com.mwu.aitokservice.ai.mapper;

import com.mwu.aitok.model.ai.domain.model.ApiKeyDO;
;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * AI API 密钥表(AiApiKey)表数据库访问层
 *
 * @author roydon
 * @since 2025-05-31 23:44:52
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyDO, Long> {


    Page<ApiKeyDO> findAllByName(String name, Pageable pageable);
}

