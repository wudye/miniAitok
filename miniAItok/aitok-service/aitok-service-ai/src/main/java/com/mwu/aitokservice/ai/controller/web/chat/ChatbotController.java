package com.mwu.aitokservice.ai.controller.web.chat;


import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.ai.vo.chat.ChatMessageVO;
import com.mwu.aitokcommon.cache.ratelimiter.core.annotation.RateLimiter;
import com.mwu.aitokcommon.cache.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import com.mwu.aitokservice.ai.controller.GetUserId;
import com.mwu.aitokservice.ai.service.IChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("v1/chat")
public class ChatbotController {

    private final IChatMessageService chatMessageService;

    private final ChatClient deepSeekChatClient;
    @GetMapping("/test")
    public String test() {
        return "test";
    }

    public record ChatRequest(@NotNull(message = "请选择对话") Long conversationId,
                              @NotNull(message = "请输入内容") String message,
                              @Schema(description = "是否携带上下文", example = "true") Boolean useContext) {
    }



    private final ChatClient openAiChatClient;
    private final ChatClient ollamaChatClient;

    public ChatbotController(IChatMessageService chatMessageService, ChatClient deepSeekChatClient, @Qualifier("openAiChatClient") ChatClient openAiChatClient,
                             @Qualifier("ollamaChatClient") ChatClient ollamaChatClient) {
        this.chatMessageService = chatMessageService;
        this.deepSeekChatClient = deepSeekChatClient;
        this.openAiChatClient = openAiChatClient;
        this.ollamaChatClient = ollamaChatClient;
    }

    @PostMapping(value = "/streama", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam("message") String message) {
        return deepSeekChatClient.prompt()
                .user(message) // 设置用户输入
                .stream()                // 发起流式调用
                .content();              // 获取并返回内容流 (Flux<String>)
    }
    @GetMapping("/openai/chat")
    public String openAIChat(@RequestParam("message") String message) {
        return openAiChatClient.prompt(message).call().content();
    }

    @GetMapping("/ollama/chat")
    public String ollamaChat(@RequestParam("message") String message) {
        return ollamaChatClient.prompt(message).call().content();
    }

    /*
作用：允许所有用户访问，无需身份验证。
适用场景：公开的 AI 聊天服务，如 Demo 版本或匿名访问。
 */
    @PermitAll

    @Operation(summary = "发送消息（流式）", description = "流式返回，响应较快")
      /*
    限流规则：2 小时内最多 10 次请求。
    keyResolver：基于客户端 IP 地址进行限流，防止单一用户过度使用。
    友好提示：中文错误消息，提升用户体验
    时间轴: 0min ── 30min ── 1hour ── 1hour30min ── 2hour

    请求1 (0min):   tryAcquire() → true  ✓  (剩余9次)
    请求2 (5min):   tryAcquire() → true  ✓  (剩余8次)
    ...
    请求10 (1hour):  tryAcquire() → true  ✓  (剩余0次)
    请求11 (1h15min): tryAcquire() → false ❌ (限流!)
    请求12 (2h):     tryAcquire() → true  ✓  (重新补充10次)
     */
    @RateLimiter(count = 10, time = 2, timeUnit = TimeUnit.HOURS, message = "请求达到上限，可以过一个时辰再来试试哦o_0", keyResolver = ClientIpRateLimiterKeyResolver.class)
    // produces：指定响应类型为 text/event-stream ，启用 SSE 协议。
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // Flux：Reactor 的响应式类型，表示 0-N 个异步数据流。
        public Flux<R<ChatMessageVO>> sendChatMessageStream(@Valid @RequestBody ChatRequest dto) {

        System.out.println("userId" + UserContext.getUserId());
        return chatMessageService.sendChatMessageStream(dto, GetUserId.getUserId());
    }
     /*
     improve version
    @PreAuthorize("hasRole('USER')")
    @RateLimiter(count = 10, time = 2, timeUnit = TimeUnit.HOURS,
             message = "请求达到上限，可以过一个时辰再来试试哦o_0",
             keyResolver = ClientIpRateLimiterKeyResolver.class,
             timeout = 30) // 添加超时时间
    public Flux<R<ChatMessageVO>> sendChatMessageStream(@Valid @RequestBody ChatRequest dto) {
    return chatMessageService.sendChatMessageStream(dto, UserContext.getUserId())
        .onErrorResume(throwable -> {
            log.error("聊天流处理异常", throwable);
            return Flux.just(R.error("服务暂时不可用，请稍后重试"));
        });
}
     */

}
