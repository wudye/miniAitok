package com.mwu.aitokservice.ai.mapper;


import com.mwu.aitok.model.ai.domain.model.ChatModelDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI 聊天模型表(AiChatModel)表数据库访问层
 *
 * @author roydon
 * @since 2025-06-02 13:41:27
 */
@Repository
public interface ChatModelMapper extends JpaRepository<ChatModelDO, Long>, Specification<ChatModelDO> {


    Page<ChatModelDO> findAll(Specification<ChatModelDO> specification, Pageable pageable);

    List<ChatModelDO> findAll(Specification<ChatModelDO> specification);

    List<ChatModelDO> findAllByStateFlagAndTypeAndPlatform(String stateFlag, String type, String platform);

    ChatModelDO findByModel(String model);
}

