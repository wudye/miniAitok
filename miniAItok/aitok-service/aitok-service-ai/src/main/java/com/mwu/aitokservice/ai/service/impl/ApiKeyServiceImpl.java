package com.mwu.aitokservice.ai.service.impl;



import com.mwu.aitiokcoomon.core.compont.SnowFlake;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitok.model.ai.domain.model.ApiKeyDO;
import com.mwu.aitok.model.ai.dto.model.ApiKeyPageDTO;
import com.mwu.aitok.model.ai.dto.model.ApiKeySaveDTO;
import com.mwu.aitok.model.ai.dto.model.ApiKeyStateDTO;
import com.mwu.aitok.model.common.enums.StateFlagEnum;
import com.mwu.aitokservice.ai.mapper.ApiKeyRepository;
import com.mwu.aitokservice.ai.service.IApiKeyService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


/**
 * AI API 密钥表(AiApiKey)表服务实现类
 *
 * @author roydon
 * @since 2025-05-31 23:44:53
 */
@Service
public class ApiKeyServiceImpl  implements IApiKeyService {
    @Resource
    private ApiKeyRepository apiKeyMapper;
    @Resource
    private SnowFlake snowFlake;

    @Override
    public Long createApiKey(ApiKeySaveDTO dto) {
        ApiKeyDO apiKey = BeanCopyUtils.copyBean(dto, ApiKeyDO.class);
        apiKey.setId(snowFlake.nextId());
        apiKeyMapper.save(apiKey);
        return apiKey.getId();
    }

    @Override
    public void updateApiKey(ApiKeySaveDTO dto) {
        ApiKeyDO apiKey = BeanCopyUtils.copyBean(dto, ApiKeyDO.class);
        apiKeyMapper.save(apiKey);
    }

    @Override
    public void deleteApiKey(Long id) {
        apiKeyMapper.deleteById(id);
    }

    @Override
    public ApiKeyDO getApiKey(Long id) {
        return apiKeyMapper.findById(id).orElse(null);
    }

    @Override
    public PageData<ApiKeyDO> getApiKeyPage(ApiKeyPageDTO pageDTO) {


        Pageable pageable = PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize()
                , Sort.by(Sort.Order.desc("createTime")
        ));
        Page<ApiKeyDO> page = apiKeyMapper.findAllByName(pageDTO.getName(), pageable);
        return PageData.page(page);
    }

    @Override
    public void updateApiKeyState(ApiKeyStateDTO dto) {
        ApiKeyDO apiKey = BeanCopyUtils.copyBean(dto, ApiKeyDO.class);
        apiKeyMapper.save(apiKey);
    }

    @Override
    public ApiKeyDO validateApiKey(Long id) {
        ApiKeyDO apiKey = getApiKey(id);
        if (StateFlagEnum.isDisable(apiKey.getStateFlag())) {
            throw new RuntimeException("密钥已被禁用");
        }
        return apiKey;
    }
}
