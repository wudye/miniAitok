// java
package com.mwu.aitokcommon.ai.config;

import org.apache.commons.lang3.StringUtils;
import com.mwu.aitiokcoomon.core.utils.spring.SpringUtils;
import com.mwu.aitokcommon.ai.factory.AiModelFactory;
import com.mwu.aitokcommon.ai.factory.AiModelFactoryImpl;
import com.mwu.aitokcommon.ai.model.baichuan.BaiChuanChatModel;

import com.mwu.aitokcommon.ai.model.deepseek.DeepSeekChatModel;
import com.mwu.aitokcommon.ai.model.doubao.DouBaoChatModel;
import com.mwu.aitokcommon.ai.model.hunyuan.HunYuanChatModel;
import com.mwu.aitokcommon.ai.model.midjourney.api.MidjourneyApi;
import com.mwu.aitokcommon.ai.model.siliconflow.SiliconFlowApiConstants;
import com.mwu.aitokcommon.ai.model.siliconflow.SiliconFlowChatModel;
import com.mwu.aitokcommon.ai.model.suno.api.SunoApi;
import com.mwu.aitokcommon.ai.model.xinghuo.XingHuoChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.autoconfigure.vectorstore.milvus.MilvusServiceClientProperties;
import org.springframework.ai.autoconfigure.vectorstore.milvus.MilvusVectorStoreProperties;
import org.springframework.ai.autoconfigure.vectorstore.qdrant.QdrantVectorStoreProperties;
import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreProperties;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * AI 自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties({NiuyinAiProperties.class,
        QdrantVectorStoreProperties.class, // 解析 Qdrant 配置
        RedisVectorStoreProperties.class, // 解析 Redis 配置
        MilvusVectorStoreProperties.class, MilvusServiceClientProperties.class // 解析 Milvus 配置
})
@Slf4j
public class NiuyinAiAutoConfiguration {

    @Bean
    public AiModelFactory aiModelFactory() {
        return new AiModelFactoryImpl();
    }

    // ========== 各种 AI Client 创建 ==========

    @Bean
    @ConditionalOnProperty(value = "niuyin.ai.deepseek.enable", havingValue = "true")
    public DeepSeekChatModel deepSeekChatModel(NiuyinAiProperties niuyinAiProperties) {
        NiuyinAiProperties.DeepSeekProperties properties = niuyinAiProperties.getDeepseek();
        return buildDeepSeekChatModel(properties);
    }

    public DeepSeekChatModel buildDeepSeekChatModel(NiuyinAiProperties.DeepSeekProperties properties) {
        if (StringUtils.isBlank(properties.getModel())) {
            properties.setModel(DeepSeekChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(DeepSeekChatModel.BASE_URL)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new DeepSeekChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "niuyin.ai.doubao.enable", havingValue = "true")
    public DouBaoChatModel douBaoChatClient(NiuyinAiProperties niuyinAiProperties) {
        NiuyinAiProperties.DouBaoProperties properties = niuyinAiProperties.getDoubao();
        return buildDouBaoChatClient(properties);
    }

    public DouBaoChatModel buildDouBaoChatClient(NiuyinAiProperties.DouBaoProperties properties) {
        if (StringUtils.isBlank(properties.getModel())) {
            properties.setModel(DouBaoChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(DouBaoChatModel.BASE_URL)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new DouBaoChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "niuyin.ai.siliconflow.enable", havingValue = "true")
    public SiliconFlowChatModel siliconFlowChatClient(NiuyinAiProperties niuyinAiProperties) {
        NiuyinAiProperties.SiliconFlowProperties properties = niuyinAiProperties.getSiliconflow();
        return buildSiliconFlowChatClient(properties);
    }

    public SiliconFlowChatModel buildSiliconFlowChatClient(NiuyinAiProperties.SiliconFlowProperties properties) {
        if (StringUtils.isBlank(properties.getModel())) {
            properties.setModel(SiliconFlowApiConstants.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(SiliconFlowApiConstants.DEFAULT_BASE_URL)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new SiliconFlowChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "niuyin.ai.hunyuan.enable", havingValue = "true")
    public HunYuanChatModel hunYuanChatClient(NiuyinAiProperties niuyinAiProperties) {
        NiuyinAiProperties.HunYuanProperties properties = niuyinAiProperties.getHunyuan();
        return buildHunYuanChatClient(properties);
    }

    public HunYuanChatModel buildHunYuanChatClient(NiuyinAiProperties.HunYuanProperties properties) {
        if (StringUtils.isBlank(properties.getModel())) {
            properties.setModel(HunYuanChatModel.MODEL_DEFAULT);
        }
        // 特殊：由于混元大模型不提供 deepseek，而是通过知识引擎，所以需要区分下 URL
        if (StringUtils.isBlank(properties.getBaseUrl())) {
            properties.setBaseUrl(StringUtils.startsWithIgnoreCase(properties.getModel(), "deepseek") ? HunYuanChatModel.DEEP_SEEK_BASE_URL : HunYuanChatModel.BASE_URL);
        }
        // 创建 OpenAiChatModel、HunYuanChatModel 对象
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(properties.getBaseUrl())
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new HunYuanChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "niuyin.ai.xinghuo.enable", havingValue = "true")
    public XingHuoChatModel xingHuoChatClient(NiuyinAiProperties niuyinAiProperties) {
        NiuyinAiProperties.XingHuoProperties properties = niuyinAiProperties.getXinghuo();
        return buildXingHuoChatClient(properties);
    }

    public XingHuoChatModel buildXingHuoChatClient(NiuyinAiProperties.XingHuoProperties properties) {
        if (StringUtils.isBlank(properties.getModel())) {
            properties.setModel(XingHuoChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(XingHuoChatModel.BASE_URL)
                        .apiKey(properties.getAppKey() + ":" + properties.getSecretKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new XingHuoChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "niuyin.ai.baichuan.enable", havingValue = "true")
    public BaiChuanChatModel baiChuanChatClient(NiuyinAiProperties niuyinAiProperties) {
        NiuyinAiProperties.BaiChuanProperties properties = niuyinAiProperties.getBaichuan();
        return buildBaiChuanChatClient(properties);
    }

    public BaiChuanChatModel buildBaiChuanChatClient(NiuyinAiProperties.BaiChuanProperties properties) {
        if (StringUtils.isBlank(properties.getModel())) {
            properties.setModel(BaiChuanChatModel.MODEL_DEFAULT);
        }
        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(BaiChuanChatModel.BASE_URL)
                        .apiKey(properties.getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(properties.getModel())
                        .temperature(properties.getTemperature())
                        .maxTokens(properties.getMaxTokens())
                        .topP(properties.getTopP())
                        .build())
                .toolCallingManager(getToolCallingManager())
                .build();
        return new BaiChuanChatModel(openAiChatModel);
    }

    @Bean
    @ConditionalOnProperty(value = "niuyin.ai.midjourney.enable", havingValue = "true")
    public MidjourneyApi midjourneyApi(NiuyinAiProperties niuyinAiProperties) {
        NiuyinAiProperties.MidjourneyProperties config = niuyinAiProperties.getMidjourney();
        return new MidjourneyApi(config.getBaseUrl(), config.getApiKey(), config.getNotifyUrl());
    }

    @Bean
    @ConditionalOnProperty(value = "niuyin.ai.suno.enable", havingValue = "true")
    public SunoApi sunoApi(NiuyinAiProperties niuyinAiProperties) {
        return new SunoApi(niuyinAiProperties.getSuno().getBaseUrl());
    }

    // ========== RAG 相关 ==========

    @Bean
    public TokenCountEstimator tokenCountEstimator() {
        return new JTokkitTokenCountEstimator();
    }

    @Bean
    public BatchingStrategy batchingStrategy() {
        return new TokenCountBatchingStrategy();
    }

    // 示例：替换 SpringUtil.getBean(...) 为 SpringUtils.getBean(...)
    private static ToolCallingManager getToolCallingManager() {
        return SpringUtils.getBean(ToolCallingManager.class);
    }

}
