package com.mwu.aitokservice.ai.service;

import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.ai.domain.model.ModelAgentDO;
import com.mwu.aitok.model.ai.dto.model.ModelRolePageDTO;
import com.mwu.aitok.model.ai.dto.model.ModelRoleSaveDTO;
import com.mwu.aitok.model.ai.dto.model.web.WebModelRolePageDTO;
import com.mwu.aitokservice.ai.controller.admin.AdminModelRoleController;

/**
 * AI 智能体表(ModelRole)表服务接口
 *
 * @author roydon
 * @since 2025-06-02 15:30:43
 */
public interface IModelRoleService {

    Long createModelRole(ModelRoleSaveDTO dto);

    void updateModelRole(ModelRoleSaveDTO dto);

    void deleteModelRole(Long id);

    ModelAgentDO getModelRole(Long id);

    PageData<ModelAgentDO> getModelRolePage(ModelRolePageDTO pageDTO);

//    void updateModelRoleState(AdminModelRoleController.ModelRoleStateDTO dto);

    PageData<ModelAgentDO> getModelRolePageForWeb(WebModelRolePageDTO pageDTO);
}
