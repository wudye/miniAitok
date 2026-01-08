package com.mwu.aitokservice.ai.service.impl;


import com.mwu.aitiokcoomon.core.compont.SnowFlake;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitok.model.ai.domain.model.ApiKeyDO;
import com.mwu.aitok.model.ai.domain.model.ChatModelDO;
import com.mwu.aitok.model.ai.dto.model.AiModelPageDTO;
import com.mwu.aitok.model.ai.dto.model.AiModelSaveDTO;
import com.mwu.aitok.model.ai.dto.model.AiModelStateDTO;
import com.mwu.aitok.model.ai.vo.model.ModelVO;
import com.mwu.aitok.model.common.enums.StateFlagEnum;
import com.mwu.aitokcommon.ai.enums.AiPlatformEnum;
import com.mwu.aitokcommon.ai.factory.AiModelFactory;
import com.mwu.aitokservice.ai.mapper.ChatModelMapper;
import com.mwu.aitokservice.ai.service.IApiKeyService;
import com.mwu.aitokservice.ai.service.IChatModelService;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 聊天模型表(AiChatModel)表服务实现类
 *
 * @author roydon
 * @since 2025-06-02 13:41:28
 */
@Service
public class ChatModelServiceImpl implements IChatModelService {
    @Resource
    private ChatModelMapper chatModelMapper;
    @Resource
    private SnowFlake snowFlake;
    @Resource
    private IApiKeyService apiKeyService;
    @Resource
    private AiModelFactory modelFactory;

    @Override
    public Long createModel(AiModelSaveDTO dto) {
        // 校验apikey
        apiKeyService.validateApiKey(dto.getKeyId());
        ChatModelDO modelDO = BeanCopyUtils.copyBean(dto, ChatModelDO.class);
        modelDO.setId(snowFlake.nextId());
        chatModelMapper.save(modelDO);
        return modelDO.getId();
    }

    @Override
    public void updateModel(AiModelSaveDTO dto) {
        // 校验apikey
        apiKeyService.validateApiKey(dto.getKeyId());
        ChatModelDO modelDO = BeanCopyUtils.copyBean(dto, ChatModelDO.class);
        chatModelMapper.save(modelDO);
    }

    @Override
    public void deleteModel(Long id) {
        chatModelMapper.deleteById(id);
    }

    @Override
    public ChatModelDO getModel(Long id) {
        return chatModelMapper.findById(id).orElse(null);
    }

    @Override
    public PageData<ChatModelDO> getModelPage(AiModelPageDTO pageDTO) {


        Specification<ChatModelDO> specification =(root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(pageDTO.getName())) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + pageDTO.getName() + "%"));
            }
            if (StringUtils.isNotBlank(pageDTO.getModel())) {
                predicates.add(criteriaBuilder.like(root.get("model"), "%" + pageDTO.getModel() + "%"));
            }
            if (StringUtils.isNotBlank(pageDTO.getKeyId())) {
                predicates.add(criteriaBuilder.equal(root.get("keyId"), pageDTO.getKeyId()));
            }
            if (StringUtils.isNotBlank(pageDTO.getPlatform())) {
                predicates.add(criteriaBuilder.equal(root.get("platform"), pageDTO.getPlatform()));
            }
            if (StringUtils.isNotBlank(pageDTO.getType())) {
                predicates.add(criteriaBuilder.equal(root.get("type"), pageDTO.getType()));
            }
            if (StringUtils.isNotBlank(pageDTO.getStateFlag())) {
                predicates.add(criteriaBuilder.equal(root.get("stateFlag"), pageDTO.getStateFlag()));
            }
            // 这里可以添加更多的动态查询条件
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(pageDTO.getPageNum(), pageDTO.getPageSize()
        , Sort.by(Sort.Order.asc("sort")));
        Page<ChatModelDO> page = chatModelMapper.findAll(specification, pageable);
        return PageData.page(page);
    }

    private ChatModelDO validateModelExists(Long id) {
        ChatModelDO model = chatModelMapper.findById(id).orElse(null);
        if (model == null) {
            throw new RuntimeException("模型不存在");
        }
        return model;
    }

    @Override
    public ChatModelDO validateModel(Long id) {
        ChatModelDO model = validateModelExists(id);
        if (StateFlagEnum.isDisable(model.getStateFlag())) {
            throw new RuntimeException("模型被禁用");
        }
        return model;
    }

    @Override
    public void updateModelState(AiModelStateDTO dto) {
        ChatModelDO modelDO = BeanCopyUtils.copyBean(dto, ChatModelDO.class);
        chatModelMapper.save(modelDO);
    }

    @Override
    public List<ChatModelDO> getModelListByStateAndTypeAndPlatform(String state, String type, String platform) {


        return  chatModelMapper.findAllByStateFlagAndTypeAndPlatform(state, type, platform);
    }

    // ========== 与 Spring AI 集成 ==========

    @Override
    public ChatModel getChatModel(Long id) {
        ChatModelDO model = validateModel(id);
        ApiKeyDO apiKey = apiKeyService.validateApiKey(model.getKeyId());
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(apiKey.getPlatform());
        return modelFactory.getOrCreateChatModel(platform, apiKey.getApiKey(), apiKey.getUrl());
    }

    @Override
    public ImageModel getImageModel(Long id) {
        ChatModelDO model = validateModel(id);
        ApiKeyDO apiKey = apiKeyService.validateApiKey(model.getKeyId());
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(apiKey.getPlatform());
        return modelFactory.getOrCreateImageModel(platform, apiKey.getApiKey(), apiKey.getUrl());
    }

    /**
     * 获得 VectorStore 对象
     *
     * @param id             编号
     * @param metadataFields 元数据的定义
     * @return VectorStore 对象
     */
    @Override
    public VectorStore getOrCreateVectorStore(Long id, Map<String, Class<?>> metadataFields) {
        // 获取模型 + 密钥
        ChatModelDO model = validateModel(id);
        ApiKeyDO apiKey = apiKeyService.validateApiKey(model.getKeyId());
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(apiKey.getPlatform());

        // 创建或获取 EmbeddingModel 对象
        EmbeddingModel embeddingModel = modelFactory.getOrCreateEmbeddingModel(platform, apiKey.getApiKey(), apiKey.getUrl(), model.getModel());

        // 创建或获取 VectorStore 对象
        return modelFactory.getOrCreateVectorStore(SimpleVectorStore.class, embeddingModel, metadataFields);
//         return modelFactory.getOrCreateVectorStore(QdrantVectorStore.class, embeddingModel, metadataFields);
//         return modelFactory.getOrCreateVectorStore(RedisVectorStore.class, embeddingModel, metadataFields);
//         return modelFactory.getOrCreateVectorStore(MilvusVectorStore.class, embeddingModel, metadataFields);
    }

    @Override
    public List<ModelVO> getModelList(String type, String platform) {



        Specification <ChatModelDO> specification =(root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("stateFlag"), StateFlagEnum.ENABLE.getCode()));
            if (StringUtils.isNotBlank(type)) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (StringUtils.isNotBlank(platform)) {
                predicates.add(criteriaBuilder.equal(root.get("platform"), platform));
            }
            query.orderBy(criteriaBuilder.asc(root.get("sort")));
            // 这里可以添加更多的动态查询条件
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        List<ChatModelDO> list = chatModelMapper.findAll(specification);

        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<ModelVO> modelVOS = BeanCopyUtils.copyBeanList(list, ModelVO.class);
        modelVOS.forEach(item -> {
            AiPlatformEnum byPlatform = AiPlatformEnum.getByPlatform(item.getPlatform());
            assert byPlatform != null;
            item.setIcon(byPlatform.getIcon());
        });
        return modelVOS;
    }

}
