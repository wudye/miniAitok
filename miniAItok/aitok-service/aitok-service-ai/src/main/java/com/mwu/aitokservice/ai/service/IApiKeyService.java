package com.mwu.aitokservice.ai.service;


import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.ai.domain.model.ApiKeyDO;
import com.mwu.aitok.model.ai.dto.model.ApiKeyPageDTO;
import com.mwu.aitok.model.ai.dto.model.ApiKeySaveDTO;
import com.mwu.aitok.model.ai.dto.model.ApiKeyStateDTO;

import java.util.List;

/**
 * AI API 密钥表(AiApiKey)表服务接口
 *
 * @author roydon
 * @since 2025-05-31 23:44:53
 */
public interface IApiKeyService {

    Long createApiKey(ApiKeySaveDTO dto);

    void updateApiKey(ApiKeySaveDTO dto);

    void deleteApiKey(Long id);

    ApiKeyDO getApiKey(Long id);

    PageData<ApiKeyDO> getApiKeyPage(ApiKeyPageDTO pageDTO);

    void updateApiKeyState(ApiKeyStateDTO dto);

    ApiKeyDO validateApiKey(Long id);

    List<ApiKeyDO> list();
}
