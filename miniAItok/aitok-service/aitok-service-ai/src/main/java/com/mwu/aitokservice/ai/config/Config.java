package com.mwu.aitokservice.ai.config;


import com.mwu.aitokcommon.ai.config.TokAiProperties;
import com.mwu.aitokcommon.ai.model.baichuan.BaiChuanChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
class Config {

//    @Bean
//    ChatClient chatClient(ChatClient.Builder builder) {
//        return builder.defaultSystem("你是一个智能机器人,你的名字叫aitok智能机器人").build();
//    }

    // 从 application.yml 中注入 DeepSeek 的配置
    @Value("${spring.ai.openai.api-key}")
    private String deepSeekApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String deepSeekBaseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String deepSeekModel;

    /**
     * 为 DeepSeek 模型创建一个 ChatClient Bean
     */
    /*
      OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(OpenAiApi.builder()
                        .baseUrl(tokAiProperties.getOpenai().getBaseUrl())
                        .apiKey(tokAiProperties.getOpenai().getApiKey())
                        .build())
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(tokAiProperties.getOpenai().getChat().getOptions().getModel())
                        .temperature(tokAiProperties.getOpenai().getChat().getOptions().getTemperature())
                        .maxTokens(tokAiProperties.getOpenai().getChat().getOptions().getMaxTokens())
                        .build())
                .build();

     */
    @Bean
    public ChatClient deepSeekChatClient() {
        // 1. 创建一个 OpenAiApi 实例，关键是指定正确的 baseUrl 和 apiKey
        OpenAiApi openAiApi = new OpenAiApi(deepSeekBaseUrl, deepSeekApiKey);

        // 2. 创建一个 OpenAiChatModel 实例
        OpenAiChatModel chatModel = new OpenAiChatModel(
                openAiApi,
                OpenAiChatOptions.builder()
                        .model(deepSeekModel)
                        .build()
        );

        // 3. 使用 ChatClient.builder 来构建并返回 ChatClient
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.create(openAiChatModel);
    }

    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel) {
        ChatClient.Builder chatClientBuilder = ChatClient.builder(ollamaChatModel);
        return chatClientBuilder.build();
    }


    @Bean
    @Primary
    public ChatClient chatClient() {
        // 默认使用DeepSeek作为主要ChatClient
        return deepSeekChatClient();
    }


    @Bean
    InMemoryChatMemory inMemoryChatMemory() {
        return new InMemoryChatMemory();
    }

}
