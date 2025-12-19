
        package com.mwu.aitokcommon.ai.model.suno.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Suno API
 *
 * 对接 Suno Proxy：<a href="https://github.com/gcui-art/suno-api">suno-api</a>
 */
@Slf4j
public class SunoApi {

    private final WebClient webClient;

    private final Predicate<HttpStatusCode> STATUS_PREDICATE = status -> !status.is2xxSuccessful();

    private final Function<Object, Function<ClientResponse, Mono<? extends Throwable>>> EXCEPTION_FUNCTION =
            reqParam -> response -> response.bodyToMono(String.class).handle((responseBody, sink) -> {
                HttpRequest request = response.request();
                log.error("[suno-api] 调用失败！请求方式:[{}]，请求地址:[{}]，请求参数:[{}]，响应数据: [{}]",
                        request.getMethod(), request.getURI(), reqParam, responseBody);
                sink.error(new IllegalStateException("[suno-api] 调用失败！"));
            });

    public SunoApi(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders((headers) -> headers.setContentType(MediaType.APPLICATION_JSON))
                .build();
    }

    public List<MusicData> generate(MusicGenerateRequest request) {
        return this.webClient.post()
                .uri("/api/generate")
                .body(Mono.just(request), MusicGenerateRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(new ParameterizedTypeReference<List<MusicData>>() {
                })
                .block();
    }

    public List<MusicData> customGenerate(MusicGenerateRequest request) {
        return this.webClient.post()
                .uri("/api/custom_generate")
                .body(Mono.just(request), MusicGenerateRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(request))
                .bodyToMono(new ParameterizedTypeReference<List<MusicData>>() {
                })
                .block();
    }

    public LyricsData generateLyrics(String prompt) {
        return this.webClient.post()
                .uri("/api/generate_lyrics")
                .body(Mono.just(new MusicGenerateRequest(prompt)), MusicGenerateRequest.class)
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(prompt))
                .bodyToMono(LyricsData.class)
                .block();
    }

    public List<MusicData> getMusicList(List<String> ids) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/get")
                        .queryParam("ids", String.join(",", ids))
                        .build())
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(ids))
                .bodyToMono(new ParameterizedTypeReference<List<MusicData>>() {
                })
                .block();
    }

    public LimitUsageData getLimitUsage() {
        return this.webClient.get()
                .uri("/api/get_limit")
                .retrieve()
                .onStatus(STATUS_PREDICATE, EXCEPTION_FUNCTION.apply(null))
                .bodyToMono(LimitUsageData.class)
                .block();
    }

    /**
     * 根据提示生成音频
     *
     * @param prompt           用于生成音乐音频的提示
     * @param tags             音乐风格
     * @param title            音乐名称
     * @param model            模型
     * @param waitAudio        false 表示后台模式，仅返回音频任务信息，需要调用 get API 获取详细的音频信息。
     *                         true 表示同步模式，API 最多等待 100s，音频生成完毕后直接返回音频链接等信息，建议在 GPT 等 agent 中使用。
     * @param makeInstrumental 指示音乐音频是否为定制，如果为 true，则从歌词生成，否则从提示生成
     */
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    public record MusicGenerateRequest(
            String prompt,
            String tags,
            String title,
            String model,
            @JsonProperty("wait_audio") boolean waitAudio,
            @JsonProperty("make_instrumental") boolean makeInstrumental
    ) {

        public MusicGenerateRequest(String prompt) {
            this(prompt, null, null, null, false, false);
        }

        public MusicGenerateRequest(String prompt, String model, boolean makeInstrumental) {
            this(prompt, null, null, model, false, makeInstrumental);
        }

        public MusicGenerateRequest(String prompt, String model, String tags, String title) {
            this(prompt, tags, title, model, false, false);
        }

    }

    public record MusicData(
            String id,
            String title,
            @JsonProperty("image_url") String imageUrl,
            String lyric,
            @JsonProperty("audio_url") String audioUrl,
            @JsonProperty("video_url") String videoUrl,
            @JsonProperty("created_at") String createdAt,
            @JsonProperty("model_name") String modelName,
            String status,
            @JsonProperty("gpt_description_prompt") String gptDescriptionPrompt,
            @JsonProperty("error_message") String errorMessage,
            String prompt,
            String type,
            String tags,
            Double duration
    ) {
    }

    public record LyricsData(
            String text,
            String title,
            String status
    ) {
    }

    public record LimitUsageData(
            @JsonProperty("credits_left") Long creditsLeft,
            String period,
            @JsonProperty("monthly_limit") Long monthlyLimit,
            @JsonProperty("monthly_usage") Long monthlyUsage
    ) {
    }

}
