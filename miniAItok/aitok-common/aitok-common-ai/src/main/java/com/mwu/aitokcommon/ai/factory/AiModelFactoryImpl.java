package com.mwu.aitokcommon.ai.factory;

import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAutoConfiguration;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.azure.ai.openai.OpenAIClientBuilder;

import com.mwu.aitiokcoomon.core.utils.spring.SpringUtils;
import com.mwu.aitokcommon.ai.config.TokAiAutoConfiguration;
import com.mwu.aitokcommon.ai.config.TokAiProperties;
import com.mwu.aitokcommon.ai.enums.AiPlatformEnum;
import com.mwu.aitokcommon.ai.model.baichuan.BaiChuanChatModel;
import com.mwu.aitokcommon.ai.model.deepseek.DeepSeekChatModel;
import com.mwu.aitokcommon.ai.model.doubao.DouBaoChatModel;
import com.mwu.aitokcommon.ai.model.hunyuan.HunYuanChatModel;
import com.mwu.aitokcommon.ai.model.midjourney.api.MidjourneyApi;
import com.mwu.aitokcommon.ai.model.siliconflow.SiliconFlowApiConstants;
import com.mwu.aitokcommon.ai.model.siliconflow.SiliconFlowChatModel;
import com.mwu.aitokcommon.ai.model.siliconflow.SiliconFlowImageApi;
import com.mwu.aitokcommon.ai.model.siliconflow.SiliconFlowImageModel;
import com.mwu.aitokcommon.ai.model.suno.api.SunoApi;
import com.mwu.aitokcommon.ai.model.xinghuo.XingHuoChatModel;
import io.micrometer.observation.ObservationRegistry;
import lombok.SneakyThrows;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiChatProperties;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiConnectionProperties;
import org.springframework.ai.autoconfigure.azure.openai.AzureOpenAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.minimax.MiniMaxAutoConfiguration;
import org.springframework.ai.autoconfigure.moonshot.MoonshotAutoConfiguration;
import org.springframework.ai.autoconfigure.ollama.OllamaAutoConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.qianfan.QianFanAutoConfiguration;
import org.springframework.ai.autoconfigure.stabilityai.StabilityAiImageAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.qdrant.QdrantVectorStoreAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreProperties;
import org.springframework.ai.autoconfigure.zhipuai.ZhiPuAiAutoConfiguration;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.MiniMaxEmbeddingModel;
import org.springframework.ai.minimax.MiniMaxEmbeddingOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.model.function.FunctionCallbackResolver;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.moonshot.MoonshotChatModel;
import org.springframework.ai.moonshot.MoonshotChatOptions;
import org.springframework.ai.moonshot.api.MoonshotApi;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.qianfan.QianFanChatModel;
import org.springframework.ai.qianfan.QianFanEmbeddingModel;
import org.springframework.ai.qianfan.QianFanEmbeddingOptions;
import org.springframework.ai.qianfan.QianFanImageModel;
import org.springframework.ai.qianfan.api.QianFanApi;
import org.springframework.ai.qianfan.api.QianFanImageApi;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.api.StabilityAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.observation.DefaultVectorStoreObservationConvention;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.zhipuai.*;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.web.client.RestClient;
import redis.clients.jedis.JedisPooled;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import static com.mwu.aitiokcoomon.core.utils.CustomCollectionUtils.convertList;
import static org.springframework.ai.retry.RetryUtils.DEFAULT_RETRY_TEMPLATE;

/**
 * AI Model 模型工厂的实现类
 */
public class AiModelFactoryImpl implements AiModelFactory {

    private static final ConcurrentHashMap<String, Object> CLIENT_CACHE = new ConcurrentHashMap<>();

    @Override
    public ChatModel getOrCreateChatModel(AiPlatformEnum platform, String apiKey, String url) {
        String cacheKey = buildClientCacheKey(ChatModel.class, platform, apiKey, url);
        return (ChatModel) CLIENT_CACHE.computeIfAbsent(cacheKey, k -> {
            // noinspection EnhancedSwitchMigration
            switch (platform) {
                case TONG_YI:
                    return buildTongYiChatModel(apiKey);
                case YI_YAN:
                    return buildYiYanChatModel(apiKey);
                case DEEP_SEEK:
                    return buildDeepSeekChatModel(apiKey);
                case DOU_BAO:
                    return buildDouBaoChatModel(apiKey);
                case HUN_YUAN:
                    return buildHunYuanChatModel(apiKey, url);
                case SILICON_FLOW:
                    return buildSiliconFlowChatModel(apiKey);
                case ZHI_PU:
                    return buildZhiPuChatModel(apiKey, url);
                case MINI_MAX:
                    return buildMiniMaxChatModel(apiKey, url);
                case MOONSHOT:
                    return buildMoonshotChatModel(apiKey, url);
                case XING_HUO:
                    return buildXingHuoChatModel(apiKey);
                case BAI_CHUAN:
                    return buildBaiChuanChatModel(apiKey);
                case OPENAI:
                    return buildOpenAiChatModel(apiKey, url);
                case AZURE_OPENAI:
                    return buildAzureOpenAiChatModel(apiKey, url);
                case OLLAMA:
                    return buildOllamaChatModel(url);
                default:
                    throw new IllegalArgumentException(String.format("未知平台(%s)", platform));
            }
        });
    }

    @Override
    public ChatModel getDefaultChatModel(AiPlatformEnum platform) {
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return SpringUtils.getBean(DashScopeChatModel.class);
            case YI_YAN:
                return SpringUtils.getBean(QianFanChatModel.class);
            case DEEP_SEEK:
                return SpringUtils.getBean(DeepSeekChatModel.class);
            case DOU_BAO:
                return SpringUtils.getBean(DouBaoChatModel.class);
            case HUN_YUAN:
                return SpringUtils.getBean(HunYuanChatModel.class);
            case SILICON_FLOW:
                return SpringUtils.getBean(SiliconFlowChatModel.class);
            case ZHI_PU:
                return SpringUtils.getBean(ZhiPuAiChatModel.class);
            case MINI_MAX:
                return SpringUtils.getBean(MiniMaxChatModel.class);
            case MOONSHOT:
                return SpringUtils.getBean(MoonshotChatModel.class);
            case XING_HUO:
                return SpringUtils.getBean(XingHuoChatModel.class);
            case BAI_CHUAN:
                return SpringUtils.getBean(AzureOpenAiChatModel.class);
            case OPENAI:
                return SpringUtils.getBean(OpenAiChatModel.class);
            case AZURE_OPENAI:
                return SpringUtils.getBean(AzureOpenAiChatModel.class);
            case OLLAMA:
                return SpringUtils.getBean(OllamaChatModel.class);
            default:
                throw new IllegalArgumentException(String.format("未知平台(%s)", platform));
        }
    }

    @Override
    public ImageModel getDefaultImageModel(AiPlatformEnum platform) {
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return SpringUtils.getBean(DashScopeImageModel.class);
            case YI_YAN:
                return SpringUtils.getBean(QianFanImageModel.class);
            case ZHI_PU:
                return SpringUtils.getBean(ZhiPuAiImageModel.class);
            case SILICON_FLOW:
                return SpringUtils.getBean(SiliconFlowImageModel.class);
            case OPENAI:
                return SpringUtils.getBean(OpenAiImageModel.class);
            case STABLE_DIFFUSION:
                return SpringUtils.getBean(StabilityAiImageModel.class);
            default:
                throw new IllegalArgumentException(String.format("未知平台(%s)", platform));
        }
    }

    @Override
    public ImageModel getOrCreateImageModel(AiPlatformEnum platform, String apiKey, String url) {
        // noinspection EnhancedSwitchMigration
        switch (platform) {
            case TONG_YI:
                return buildTongYiImagesModel(apiKey);
            case YI_YAN:
                return buildQianFanImageModel(apiKey);
            case ZHI_PU:
                return buildZhiPuAiImageModel(apiKey, url);
            case OPENAI:
                return buildOpenAiImageModel(apiKey, url);
            case SILICON_FLOW:
                return buildSiliconFlowImageModel(apiKey, url);
            case STABLE_DIFFUSION:
                return buildStabilityAiImageModel(apiKey, url);
            default:
                throw new IllegalArgumentException(String.format("未知平台(%s)", platform));
        }
    }

    @Override
    public MidjourneyApi getOrCreateMidjourneyApi(String apiKey, String url) {
        String cacheKey = buildClientCacheKey(MidjourneyApi.class, AiPlatformEnum.MIDJOURNEY.getPlatform(), apiKey,
                url);
        return (MidjourneyApi) CLIENT_CACHE.computeIfAbsent(cacheKey, k -> {
            TokAiProperties.MidjourneyProperties properties = SpringUtils.getBean(TokAiProperties.class)
                    .getMidjourney();
            return new MidjourneyApi(url, apiKey, properties.getNotifyUrl());
        });
    }

    @Override
    public SunoApi getOrCreateSunoApi(String apiKey, String url) {
        String cacheKey = buildClientCacheKey(SunoApi.class, AiPlatformEnum.SUNO.getPlatform(), apiKey, url);
        return (SunoApi) CLIENT_CACHE.computeIfAbsent(cacheKey, k -> new SunoApi(url));
    }

    @Override
    @SuppressWarnings("EnhancedSwitchMigration")
    public EmbeddingModel getOrCreateEmbeddingModel(AiPlatformEnum platform, String apiKey, String url, String model) {
        String cacheKey = buildClientCacheKey(EmbeddingModel.class, platform, apiKey, url, model);
        return (EmbeddingModel) CLIENT_CACHE.computeIfAbsent(cacheKey, k -> {
            switch (platform) {
                case TONG_YI:
                    return buildTongYiEmbeddingModel(apiKey, model);
                case YI_YAN:
                    return buildYiYanEmbeddingModel(apiKey, model);
                case ZHI_PU:
                    return buildZhiPuEmbeddingModel(apiKey, url, model);
                case MINI_MAX:
                    return buildMiniMaxEmbeddingModel(apiKey, url, model);
                case OPENAI:
                    return buildOpenAiEmbeddingModel(apiKey, url, model);
                case AZURE_OPENAI:
                    return buildAzureOpenAiEmbeddingModel(apiKey, url, model);
                case OLLAMA:
                    return buildOllamaEmbeddingModel(url, model);
                default:
                    throw new IllegalArgumentException(String.format("未知平台(%s)", platform));
            }
        });
    }

    @Override
    public VectorStore getOrCreateVectorStore(Class<? extends VectorStore> type,
                                              EmbeddingModel embeddingModel,
                                              Map<String, Class<?>> metadataFields) {
        String cacheKey = buildClientCacheKey(VectorStore.class, embeddingModel, type);
        return (VectorStore) CLIENT_CACHE.computeIfAbsent(cacheKey, k -> {
            if (type == SimpleVectorStore.class) {
                return buildSimpleVectorStore(embeddingModel);
            }
            if (type == RedisVectorStore.class) {
                return buildRedisVectorStore(embeddingModel, metadataFields);
            }
            throw new IllegalArgumentException(String.format("未知类型(%s)", type));
        });
    }

    private static String buildClientCacheKey(Class<?> clazz, Object... params) {
        if (ArrayUtils.isEmpty(params)) {
            return clazz.getName();
        }
        return String.format("%s#%s", clazz.getName(), StringUtils.join(params, "_"));
    }

    // ========== 各种创建 spring-ai 客户端的方法 ==========

    /**
     * 可参考 {@link DashScopeAutoConfiguration} 的 dashscopeChatModel 方法
     */
    private static DashScopeChatModel buildTongYiChatModel(String key) {
        DashScopeApi dashScopeApi = new DashScopeApi(key);
        DashScopeChatOptions options = DashScopeChatOptions.builder().withModel(DashScopeApi.DEFAULT_CHAT_MODEL)
                .withTemperature(0.7).build();
        return new DashScopeChatModel(dashScopeApi, options, getFunctionCallbackResolver(), DEFAULT_RETRY_TEMPLATE);
    }

    /**
     * 可参考 {@link DashScopeAutoConfiguration} 的 dashScopeImageModel 方法
     */
    private static DashScopeImageModel buildTongYiImagesModel(String key) {
        DashScopeImageApi dashScopeImageApi = new DashScopeImageApi(key);
        return new DashScopeImageModel(dashScopeImageApi);
    }

    /**
     * 可参考 {@link QianFanAutoConfiguration} 的 qianFanChatModel 方法
     */
    private static QianFanChatModel buildYiYanChatModel(String key) {
        String[] keys = StringUtils.split(key, '|');
        Assert.isTrue(keys.length == 2, "YiYanChatClient 的密钥需要 (appKey|secretKey) 格式");
        String appKey = keys[0];
        String secretKey = keys[1];
        QianFanApi qianFanApi = new QianFanApi(appKey, secretKey);
        return new QianFanChatModel(qianFanApi);
    }

    /**
     * 可参考 {@link QianFanAutoConfiguration} 的 qianFanImageModel 方法
     */
    private QianFanImageModel buildQianFanImageModel(String key) {
        String[] keys = StringUtils.split(key, '|');
        Assert.isTrue(keys.length == 2, "YiYanChatClient 的密钥需要 (appKey|secretKey) 格式");
        String appKey = keys[0];
        String secretKey = keys[1];
        QianFanImageApi qianFanApi = new QianFanImageApi(appKey, secretKey);
        return new QianFanImageModel(qianFanApi);
    }

    /**
     * 可参考 {@link TokAiAutoConfiguration#deepSeekChatModel(TokAiProperties)}
     */
    private static DeepSeekChatModel buildDeepSeekChatModel(String apiKey) {
        TokAiProperties.DeepSeekProperties properties = new TokAiProperties.DeepSeekProperties()
                .setApiKey(apiKey);
        return new TokAiAutoConfiguration().buildDeepSeekChatModel(properties);
    }

    /**
     * 可参考 {@link TokAiAutoConfiguration#douBaoChatClient(TokAiProperties)}
     */
    private ChatModel buildDouBaoChatModel(String apiKey) {
        TokAiProperties.DouBaoProperties properties = new TokAiProperties.DouBaoProperties()
                .setApiKey(apiKey);
        return new TokAiAutoConfiguration().buildDouBaoChatClient(properties);
    }

    /**
     * 可参考 {@link TokAiAutoConfiguration#hunYuanChatClient(TokAiProperties)}
     */
    private ChatModel buildHunYuanChatModel(String apiKey, String url) {
        TokAiProperties.HunYuanProperties properties = new TokAiProperties.HunYuanProperties()
                .setBaseUrl(url).setApiKey(apiKey);
        return new TokAiAutoConfiguration().buildHunYuanChatClient(properties);
    }

    /**
     * 可参考 {@link TokAiAutoConfiguration#siliconFlowChatClient(TokAiProperties)}
     */
    private ChatModel buildSiliconFlowChatModel(String apiKey) {
        TokAiProperties.SiliconFlowProperties properties = new TokAiProperties.SiliconFlowProperties()
                .setApiKey(apiKey);
        return new TokAiAutoConfiguration().buildSiliconFlowChatClient(properties);
    }

    /**
     * 可参考 {@link ZhiPuAiAutoConfiguration} 的 zhiPuAiChatModel 方法
     */
    private ZhiPuAiChatModel buildZhiPuChatModel(String apiKey, String url) {
        ZhiPuAiApi zhiPuAiApi = StringUtils.isBlank(url) ? new ZhiPuAiApi(apiKey)
                : new ZhiPuAiApi(url, apiKey);
        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder().model(ZhiPuAiApi.DEFAULT_CHAT_MODEL).temperature(0.7).build();
        return new ZhiPuAiChatModel(zhiPuAiApi, options, getFunctionCallbackResolver(), DEFAULT_RETRY_TEMPLATE);
    }

    /**
     * 可参考 {@link ZhiPuAiAutoConfiguration} 的 zhiPuAiImageModel 方法
     */
    private ZhiPuAiImageModel buildZhiPuAiImageModel(String apiKey, String url) {
        ZhiPuAiImageApi zhiPuAiApi = StringUtils.isBlank(url) ? new ZhiPuAiImageApi(apiKey)
                : new ZhiPuAiImageApi(url, apiKey, RestClient.builder());
        return new ZhiPuAiImageModel(zhiPuAiApi);
    }

    /**
     * 可参考 {@link MiniMaxAutoConfiguration} 的 miniMaxChatModel 方法
     */
    private MiniMaxChatModel buildMiniMaxChatModel(String apiKey, String url) {
        MiniMaxApi miniMaxApi = StringUtils.isBlank(url) ? new MiniMaxApi(apiKey)
                : new MiniMaxApi(url, apiKey);
        MiniMaxChatOptions options = MiniMaxChatOptions.builder().model(MiniMaxApi.DEFAULT_CHAT_MODEL).temperature(0.7).build();
        return new MiniMaxChatModel(miniMaxApi, options, getFunctionCallbackResolver(), DEFAULT_RETRY_TEMPLATE);
    }

    /**
     * 可参考 {@link MoonshotAutoConfiguration} 的 moonshotChatModel 方法
     */
    private MoonshotChatModel buildMoonshotChatModel(String apiKey, String url) {
        MoonshotApi moonshotApi = StringUtils.isBlank(url) ? new MoonshotApi(apiKey)
                : new MoonshotApi(url, apiKey);
        MoonshotChatOptions options = MoonshotChatOptions.builder().model(MoonshotApi.DEFAULT_CHAT_MODEL).build();
        return new MoonshotChatModel(moonshotApi, options, getFunctionCallbackResolver(), DEFAULT_RETRY_TEMPLATE);
    }

    /**
     * 可参考 {@link TokAiAutoConfiguration#xingHuoChatClient(TokAiProperties)}
     */
    private static XingHuoChatModel buildXingHuoChatModel(String key) {
        String[] keys = StringUtils.split(key, '|');
        Assert.isTrue(keys.length == 2, "XingHuoChatClient 的密钥需要 (appKey|secretKey) 格式");
        TokAiProperties.XingHuoProperties properties = new TokAiProperties.XingHuoProperties()
                .setAppKey(keys[0]).setSecretKey(keys[1]);
        return new TokAiAutoConfiguration().buildXingHuoChatClient(properties);
    }

    /**
     * 可参考 {@link TokAiAutoConfiguration#baiChuanChatClient(TokAiProperties)}
     */
    private BaiChuanChatModel buildBaiChuanChatModel(String apiKey) {
        TokAiProperties.BaiChuanProperties properties = new TokAiProperties.BaiChuanProperties()
                .setApiKey(apiKey);
        return new TokAiAutoConfiguration().buildBaiChuanChatClient(properties);
    }

    /**
     * 可参考 {@link OpenAiAutoConfiguration} 的 openAiChatModel 方法
     */
    private static OpenAiChatModel buildOpenAiChatModel(String openAiToken, String url) {
        url = StringUtils.defaultIfBlank(url, OpenAiApiConstants.DEFAULT_BASE_URL);
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(url).apiKey(openAiToken).build();
        return OpenAiChatModel.builder().openAiApi(openAiApi).toolCallingManager(getToolCallingManager()).build();
    }

    // TODO @芋艿：手头暂时没密钥，使用建议再测试下

    /**
     * 可参考 {@link AzureOpenAiAutoConfiguration}
     */
    private static AzureOpenAiChatModel buildAzureOpenAiChatModel(String apiKey, String url) {
        AzureOpenAiAutoConfiguration azureOpenAiAutoConfiguration = new AzureOpenAiAutoConfiguration();
        // 创建 OpenAIClient 对象
        AzureOpenAiConnectionProperties connectionProperties = new AzureOpenAiConnectionProperties();
        connectionProperties.setApiKey(apiKey);
        connectionProperties.setEndpoint(url);
        OpenAIClientBuilder openAIClient = azureOpenAiAutoConfiguration.openAIClientBuilder(connectionProperties, null);
        // 获取 AzureOpenAiChatProperties 对象
        AzureOpenAiChatProperties chatProperties = SpringUtils.getBean(AzureOpenAiChatProperties.class);
        return azureOpenAiAutoConfiguration.azureOpenAiChatModel(openAIClient, chatProperties,
                getToolCallingManager(), null, null);
    }

    /**
     * 可参考 {@link OpenAiAutoConfiguration} 的 openAiImageModel 方法
     */
    private OpenAiImageModel buildOpenAiImageModel(String openAiToken, String url) {
        url = StringUtils.defaultIfBlank(url, OpenAiApiConstants.DEFAULT_BASE_URL);
        OpenAiImageApi openAiApi = OpenAiImageApi.builder().baseUrl(url).apiKey(openAiToken).build();
        return new OpenAiImageModel(openAiApi);
    }

    /**
     * 创建 SiliconFlowImageModel 对象
     */
    private SiliconFlowImageModel buildSiliconFlowImageModel(String apiToken, String url) {
        url = StringUtils.defaultIfBlank(url, SiliconFlowApiConstants.DEFAULT_BASE_URL);
        SiliconFlowImageApi openAiApi = new SiliconFlowImageApi(url, apiToken);
        return new SiliconFlowImageModel(openAiApi);
    }

    /**
     * 可参考 {@link OllamaAutoConfiguration} 的 ollamaApi 方法
     */
    private static OllamaChatModel buildOllamaChatModel(String url) {
        OllamaApi ollamaApi = new OllamaApi(url);
        return OllamaChatModel.builder().ollamaApi(ollamaApi).toolCallingManager(getToolCallingManager()).build();
    }

    /**
     * 可参考 {@link StabilityAiImageAutoConfiguration} 的 stabilityAiImageModel 方法
     */
    private StabilityAiImageModel buildStabilityAiImageModel(String apiKey, String url) {
        url = StringUtils.defaultIfBlank(url, StabilityAiApi.DEFAULT_BASE_URL);
        StabilityAiApi stabilityAiApi = new StabilityAiApi(apiKey, StabilityAiApi.DEFAULT_IMAGE_MODEL, url);
        return new StabilityAiImageModel(stabilityAiApi);
    }

    // ========== 各种创建 EmbeddingModel 的方法 ==========

    /**
     * 可参考 {@link DashScopeAutoConfiguration} 的 dashscopeEmbeddingModel 方法
     */
    private DashScopeEmbeddingModel buildTongYiEmbeddingModel(String apiKey, String model) {
        DashScopeApi dashScopeApi = new DashScopeApi(apiKey);
        DashScopeEmbeddingOptions dashScopeEmbeddingOptions = DashScopeEmbeddingOptions.builder().withModel(model).build();
        return new DashScopeEmbeddingModel(dashScopeApi, MetadataMode.EMBED, dashScopeEmbeddingOptions);
    }

    /**
     * 可参考 {@link ZhiPuAiAutoConfiguration} 的 zhiPuAiEmbeddingModel 方法
     */
    private ZhiPuAiEmbeddingModel buildZhiPuEmbeddingModel(String apiKey, String url, String model) {
        ZhiPuAiApi zhiPuAiApi = StringUtils.isBlank(url) ? new ZhiPuAiApi(apiKey)
                : new ZhiPuAiApi(url, apiKey);
        ZhiPuAiEmbeddingOptions zhiPuAiEmbeddingOptions = ZhiPuAiEmbeddingOptions.builder().model(model).build();
        return new ZhiPuAiEmbeddingModel(zhiPuAiApi, MetadataMode.EMBED, zhiPuAiEmbeddingOptions);
    }

    /**
     * 可参考 {@link MiniMaxAutoConfiguration} 的 miniMaxEmbeddingModel 方法
     */
    private EmbeddingModel buildMiniMaxEmbeddingModel(String apiKey, String url, String model) {
        MiniMaxApi miniMaxApi = StringUtils.isBlank(url) ? new MiniMaxApi(apiKey)
                : new MiniMaxApi(url, apiKey);
        MiniMaxEmbeddingOptions miniMaxEmbeddingOptions = MiniMaxEmbeddingOptions.builder().model(model).build();
        return new MiniMaxEmbeddingModel(miniMaxApi, MetadataMode.EMBED, miniMaxEmbeddingOptions);
    }

    /**
     * 可参考 {@link QianFanAutoConfiguration} 的 qianFanEmbeddingModel 方法
     */
    private QianFanEmbeddingModel buildYiYanEmbeddingModel(String key, String model) {
        String[] keys = StringUtils.split(key, '|');
        Assert.isTrue(keys.length == 2, "YiYanChatClient 的密钥需要 (appKey|secretKey) 格式");
        String appKey = keys[0];
        String secretKey = keys[1];
        QianFanApi qianFanApi = new QianFanApi(appKey, secretKey);
        QianFanEmbeddingOptions qianFanEmbeddingOptions = QianFanEmbeddingOptions.builder().model(model).build();
        return new QianFanEmbeddingModel(qianFanApi, MetadataMode.EMBED, qianFanEmbeddingOptions);
    }

    private OllamaEmbeddingModel buildOllamaEmbeddingModel(String url, String model) {
        OllamaApi ollamaApi = new OllamaApi(url);
        OllamaOptions ollamaOptions = OllamaOptions.builder().model(model).build();
        return OllamaEmbeddingModel.builder().ollamaApi(ollamaApi).defaultOptions(ollamaOptions).build();
    }

    /**
     * 可参考 {@link OpenAiAutoConfiguration} 的 openAiEmbeddingModel 方法
     */
    private OpenAiEmbeddingModel buildOpenAiEmbeddingModel(String openAiToken, String url, String model) {
        url = StringUtils.defaultIfBlank(url, OpenAiApiConstants.DEFAULT_BASE_URL);
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(url).apiKey(openAiToken).build();
        OpenAiEmbeddingOptions openAiEmbeddingProperties = OpenAiEmbeddingOptions.builder().model(model).build();
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, openAiEmbeddingProperties);
    }

    // TODO @芋艿：手头暂时没密钥，使用建议再测试下

    /**
     * 可参考 {@link AzureOpenAiAutoConfiguration} 的 azureOpenAiEmbeddingModel 方法
     */
    private AzureOpenAiEmbeddingModel buildAzureOpenAiEmbeddingModel(String apiKey, String url, String model) {
        AzureOpenAiAutoConfiguration azureOpenAiAutoConfiguration = new AzureOpenAiAutoConfiguration();
        // 创建 OpenAIClient 对象
        AzureOpenAiConnectionProperties connectionProperties = new AzureOpenAiConnectionProperties();
        connectionProperties.setApiKey(apiKey);
        connectionProperties.setEndpoint(url);
        OpenAIClientBuilder openAIClient = azureOpenAiAutoConfiguration.openAIClientBuilder(connectionProperties, null);
        // 获取 AzureOpenAiChatProperties 对象
        AzureOpenAiEmbeddingProperties embeddingProperties = SpringUtils.getBean(AzureOpenAiEmbeddingProperties.class);
        return azureOpenAiAutoConfiguration.azureOpenAiEmbeddingModel(openAIClient, embeddingProperties,
                null, null);
    }

    // ========== 各种创建 VectorStore 的方法 ==========

    /**
     * 注意：仅适合本地测试使用，生产建议还是使用 Qdrant、Milvus 等
     */
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private SimpleVectorStore buildSimpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        // 启动加载
        File file = new File(String.format("%s/vector_store/simple_%s.json",
                System.getProperty("user.home"), embeddingModel.getClass().getSimpleName()));
        if (!file.exists()) {
            FileUtils.forceMkdirParent(file);
            file.createNewFile();
        } else if (file.length() > 0) {
            vectorStore.load(file);
        }
        // 定时持久化，每分钟一次
        Timer timer = new Timer("SimpleVectorStoreTimer-" + file.getAbsolutePath());
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                vectorStore.save(file);
            }

        }, Duration.ofMinutes(1).toMillis(), Duration.ofMinutes(1).toMillis());
        // 关闭时，进行持久化
        Runtime.getRuntime().addShutdownHook(new Thread(() -> vectorStore.save(file)));
        return vectorStore;
    }

    /**
     * 参考 {@link QdrantVectorStoreAutoConfiguration} 的 vectorStore 方法
     */
//    @SneakyThrows
//    private QdrantVectorStore buildQdrantVectorStore(EmbeddingModel embeddingModel) {
//        QdrantVectorStoreAutoConfiguration configuration = new QdrantVectorStoreAutoConfiguration();
//        QdrantVectorStoreProperties properties = SpringUtils.getBean(QdrantVectorStoreProperties.class);
//        // 参考 QdrantVectorStoreAutoConfiguration 实现，创建 QdrantClient 对象
//        QdrantGrpcClient.Builder grpcClientBuilder = QdrantGrpcClient.newBuilder(
//                properties.getHost(), properties.getPort(), properties.isUseTls());
//        if (StringUtils.isNotBlank(properties.getApiKey())) {
//            grpcClientBuilder.withApiKey(properties.getApiKey());
//        }
//        QdrantClient qdrantClient = new QdrantClient(grpcClientBuilder.build());
//        // 创建 QdrantVectorStore 对象
//        QdrantVectorStore vectorStore = configuration.vectorStore(embeddingModel, properties, qdrantClient,
//                getObservationRegistry(), getCustomObservationConvention(), getBatchingStrategy());
//        // 初始化索引
//        vectorStore.afterPropertiesSet();
//        return vectorStore;
//    }

    /**
     * 参考 {@link RedisVectorStoreAutoConfiguration} 的 vectorStore 方法
     */
    private RedisVectorStore buildRedisVectorStore(EmbeddingModel embeddingModel,
                                                   Map<String, Class<?>> metadataFields) {
        // 创建 JedisPooled 对象
        RedisProperties redisProperties = SpringUtils.getBean(RedisProperties.class);
        JedisPooled jedisPooled = new JedisPooled(redisProperties.getHost(), redisProperties.getPort());
        // 创建 RedisVectorStoreProperties 对象
        RedisVectorStoreAutoConfiguration configuration = new RedisVectorStoreAutoConfiguration();
        RedisVectorStoreProperties properties = SpringUtils.getBean(RedisVectorStoreProperties.class);
        RedisVectorStore redisVectorStore = RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(properties.getIndex()).prefix(properties.getPrefix())
                .initializeSchema(properties.isInitializeSchema())
                .metadataFields(convertList(metadataFields.entrySet(), entry -> {
                    String fieldName = entry.getKey();
                    Class<?> fieldType = entry.getValue();
                    if (Number.class.isAssignableFrom(fieldType)) {
                        return RedisVectorStore.MetadataField.numeric(fieldName);
                    }
                    if (Boolean.class.isAssignableFrom(fieldType)) {
                        return RedisVectorStore.MetadataField.tag(fieldName);
                    }
                    return RedisVectorStore.MetadataField.text(fieldName);
                }))
                .observationRegistry(getObservationRegistry().getObject())
                .customObservationConvention(getCustomObservationConvention().getObject())
                .batchingStrategy(getBatchingStrategy())
                .build();
        // 初始化索引
        redisVectorStore.afterPropertiesSet();
        return redisVectorStore;
    }

    private static ObjectProvider<ObservationRegistry> getObservationRegistry() {
        return new ObjectProvider<>() {

            @Override
            public ObservationRegistry getObject() throws BeansException {
                return SpringUtils.getBean(ObservationRegistry.class);
            }

        };
    }

    private static ObjectProvider<VectorStoreObservationConvention> getCustomObservationConvention() {
        return new ObjectProvider<>() {
            @Override
            public VectorStoreObservationConvention getObject() throws BeansException {
                return new DefaultVectorStoreObservationConvention();
            }
        };
    }

    private static BatchingStrategy getBatchingStrategy() {
        return SpringUtils.getBean(BatchingStrategy.class);
    }

    private static ToolCallingManager getToolCallingManager() {
        return SpringUtils.getBean(ToolCallingManager.class);
    }

    private static FunctionCallbackResolver getFunctionCallbackResolver() {
        return SpringUtils.getBean(FunctionCallbackResolver.class);
    }

}
