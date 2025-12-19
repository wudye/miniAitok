package com.mwu.aitokservice.ai.service;


import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.ai.domain.model.ModelAgentCategoryDO;
import com.mwu.aitok.model.ai.dto.model.ModelAgentCategoryPageDTO;

import java.util.List;

/**
 * AI 智能体分类表(AiModelAgentCategory)表服务接口
 *
 * @author roydon
 * @since 2025-06-13 10:21:50
 */
public interface IModelAgentCategoryService{

    PageData<ModelAgentCategoryDO> getModelAgentCategoryPage(ModelAgentCategoryPageDTO pageDTO);

    ModelAgentCategoryDO getModelAgentCategory(Long id);

    Long createModelAgentCategory(ModelAgentCategoryDO dto);

    void updateModelAgentCategory(ModelAgentCategoryDO dto);

    void deleteModelAgentCategory(Long id);

    List<ModelAgentCategoryDO> list();
}
