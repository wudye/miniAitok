package com.mwu.aitokservice.ai.service.impl;


import com.mwu.aitiokcoomon.core.compont.SnowFlake;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.ai.domain.knowledge.KnowledgeDO;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitokservice.ai.mapper.KnowledgeMapper;
import com.mwu.aitokservice.ai.service.IKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AI 知识库表(Knowledge)表服务实现类
 *
 * @author roydon
 * @since 2025-06-03 22:03:27
 */
@RequiredArgsConstructor
@Service
public class KnowledgeServiceImpl implements IKnowledgeService {
    private final KnowledgeMapper knowledgeMapper;
    private final SnowFlake snowFlake;

    @Override
    public KnowledgeDO validateKnowledgeExists(Long knowledgeId) {
        KnowledgeDO knowledgeDO = knowledgeMapper.findById(knowledgeId).orElse(null);
        if (knowledgeDO == null) {
            throw new RuntimeException("知识库不存在");
        }
        return knowledgeDO;
    }

    /**
     * 创建知识库
     */
    @Override
    public Long createKnowledge(KnowledgeDO dto) {
        dto.setId(snowFlake.nextId());
        dto.setUserId(UserContext.getUserId());
        dto.setCreateBy(UserContext.getUser().getUserName());
        dto.setCreateTime(LocalDateTime.now());
        knowledgeMapper.save(dto);
        return dto.getId();
    }

    @Override
    public Long editKnowledge(KnowledgeDO dto) {
        dto.setUpdateBy(UserContext.getUser().getUserName());
        dto.setUpdateTime(LocalDateTime.now());
        knowledgeMapper.save(dto);
        return dto.getId();
    }

    /**
     * 获取知识库分页列表
     *
     */
    @Override
    public PageData<KnowledgeDO> knowledgeList(PageDTO pageDTO) {

        Pageable pageable = PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize());
        Page<KnowledgeDO> page = knowledgeMapper.findAllByUserId(UserContext.getUserId(), pageable);
        return PageData.page(page);
    }
}
