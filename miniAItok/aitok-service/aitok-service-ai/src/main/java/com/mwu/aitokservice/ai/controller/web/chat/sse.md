SSE (Server-Sent Events) 是一种服务器推送技术，允许服务器主动向客户端推送数据。

SSE 协议详解
1. 基本概念
   客户端 ←──── 服务器主动推送数据 ────→ 服务器
   单向通信：只能是服务器推送给客户端
   持久连接：HTTP长连接，持续接收数据
   文本格式：纯文本数据流，格式简单
2. 在你的代码中的作用

   @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<R<ChatMessageVO>> sendChatMessageStream(@Valid @RequestBody ChatRequest dto) {
   return chatMessageService.sendChatMessageStream(dto, UserContext.getUserId());
   }
   关键点：

produces = MediaType.TEXT_EVENT_STREAM_VALUE ：声明返回SSE格式
Flux<R<ChatMessageVO>> ：响应式流，支持连续推送
实现AI聊天的实时打字效果
3. SSE 数据格式
   服务端推送的数据格式：

data: {"message": "你好"}

data: {"message": "我是AI助手"}

data: {"message": "有什么可以帮助你的吗？"}
格式说明：

data: 前缀表示数据行
每个事件用 \n\n 分隔
支持事件ID、重试时间等元数据
4. 与其他技术对比
   技术	通信方向	连接方式	复杂度	适用场景
   SSE	服务器→客户端	HTTP长连接	简单	实时通知、流式响应
   WebSocket	双向通信	WebSocket协议	中等	聊天、游戏
   轮询	客户端→服务器	短连接	简单	低频数据更新
   Webhook	服务器→客户端	HTTP回调	中等	异步通知