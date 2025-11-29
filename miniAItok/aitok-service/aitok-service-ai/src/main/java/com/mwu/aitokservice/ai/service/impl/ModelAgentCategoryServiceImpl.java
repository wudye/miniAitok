package com.mwu.aitokservice.ai.service.impl;


import com.mwu.aitiokcoomon.core.compont.SnowFlake;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.ai.domain.model.ModelAgentCategoryDO;
import com.mwu.aitok.model.ai.dto.model.ModelAgentCategoryPageDTO;
import com.mwu.aitokservice.ai.mapper.ModelAgentCategoryMapper;
import com.mwu.aitokservice.ai.service.IModelAgentCategoryService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AI 智能体分类表(AiModelAgentCategory)表服务实现类
 *
 * @author roydon
 * @since 2025-06-13 10:21:50
 */
@Service
public class ModelAgentCategoryServiceImpl implements IModelAgentCategoryService {
    @Resource
    private ModelAgentCategoryMapper modelAgentCategoryMapper;
    @Resource
    private SnowFlake snowFlake;

    @Override
    public PageData<ModelAgentCategoryDO> getModelAgentCategoryPage(ModelAgentCategoryPageDTO pageDTO) {


        Pageable pageable = PageRequest.of(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<ModelAgentCategoryDO> page = modelAgentCategoryMapper.findAllByName(pageDTO.getName(), pageable);
        return PageData.page(page);
    }

    @Override
    public ModelAgentCategoryDO getModelAgentCategory(Long id) {

        return modelAgentCategoryMapper.findById(id).orElse(null);
    }

    @Override
    public Long createModelAgentCategory(ModelAgentCategoryDO dto) {
        dto.setId(snowFlake.nextId());
        dto.setCreateBy(UserContext.getUser().getUserName());
        dto.setCreateTime(LocalDateTime.now());
        modelAgentCategoryMapper.save(dto);
        return dto.getId();
    }

    @Override
    public void updateModelAgentCategory(ModelAgentCategoryDO dto) {
        dto.setUpdateBy(UserContext.getUser().getUserName());
        dto.setUpdateTime(LocalDateTime.now());
        modelAgentCategoryMapper.save(dto);
    }

    @Override
    public void deleteModelAgentCategory(Long id) {
        modelAgentCategoryMapper.deleteById(id);
    }
}
