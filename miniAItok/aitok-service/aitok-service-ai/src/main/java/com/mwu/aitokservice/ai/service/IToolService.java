package com.mwu.aitokservice.ai.service;


import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.ai.domain.model.ToolDO;
import com.mwu.aitok.model.ai.dto.model.AiToolSaveDTO;
import com.mwu.aitok.model.ai.dto.model.ToolPageDTO;
import com.mwu.aitokservice.ai.controller.admin.AdminToolController;

import java.util.List;

/**
 * AI 工具表(Tool)表服务接口
 *
 * @author roydon
 * @since 2025-06-05 16:02:47
 */
public interface IToolService  {

    Long createTool(AiToolSaveDTO dto);

    void updateTool(AiToolSaveDTO dto);

    void deleteTool(Long id);

    ToolDO getTool(Long id);

    PageData<ToolDO> getToolPage(ToolPageDTO pageDTO);

    List<ToolDO> getToolListByState(String state);

    void updateToolState(AdminToolController.ToolStateDTO dto);

    List<ToolDO> getToolList(List<Long> toolIds);
}
