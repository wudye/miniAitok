# AI聊天演示服务

这是一个基于Spring AI的多AI提供商聊天演示服务，支持百度文心一言、智谱AI、OpenAI等多个AI服务。

## 🚀 快速开始

### 1. 启动应用
```bash
# 使用演示配置文件启动
mvn spring-boot:run -Dspring-boot.run.profiles=demo

# 或者直接运行DemoAiApplication类
```

### 2. 访问服务
应用启动后访问: http://localhost:8080/demo/test

## 📚 API接口

### 基础信息
- **获取支持的AI提供商**
  ```bash
  GET http://localhost:8080/demo/providers
  ```

### 聊天接口
- **使用百度文心一言**
  ```bash
  POST http://localhost:8080/demo/qianfan
  Content-Type: application/json
  
  {
    "message": "你好，请介绍一下自己"
  }
  ```

- **使用智谱AI**
  ```bash
  POST http://localhost:8080/demo/zhipu
  Content-Type: application/json
  
  {
    "message": "请解释什么是人工智能"
  }
  ```

- **使用OpenAI**
  ```bash
  POST http://localhost:8080/demo/openai
  Content-Type: application/json
  
  {
    "message": "Tell me about AI"
  }
  ```

- **流式聊天（推荐）**
  ```bash
  POST http://localhost:8080/demo/stream
  Content-Type: application/json
  
  {
    "message": "请写一首关于春天的诗"
  }
  ```

### 标准API接口
- **同步聊天**
  ```bash
  POST http://localhost:8080/api/ai/chat/sync
  Content-Type: application/json
  
  {
    "provider": "QIANFAN",
    "message": "你好"
  }
  ```

- **流式聊天**
  ```bash
  POST http://localhost:8080/api/ai/chat/stream
  Content-Type: application/json
  
  {
    "provider": "QIANFAN",
    "message": "你好"
  }
  ```

## 🔧 配置说明

应用使用 `application-demo.yml` 配置文件，包含了您配置的所有AI服务：

- **百度文心一言 (Qianfan)**: ✅ 已配置
- **智谱AI (ZhipuAI)**: ✅ 已配置  
- **OpenAI**: ✅ 已配置（使用代理）
- **Azure OpenAI**: ⚙️ 已配置（需要更新API key）
- **阿里云通义千问**: ⚙️ 已配置
- **月之暗面 (KIMI)**: ⚙️ 已配置（需要更新API key）
- **其他服务**: ⚙️ 已配置基础结构

## 📁 项目结构

```
com.mwu.aitokservice.ai.tempDemoAi/
├── DemoAiApplication.java          # 启动类
├── config/
│   └── AiModelConfig.java          # AI模型配置
├── controller/
│   ├── AiChatController.java       # 标准API控制器
│   └── DemoChatController.java     # 演示API控制器
├── service/
│   └── AiChatService.java          # 聊天服务
├── dto/
│   ├── ChatRequest.java            # 请求DTO
│   └── ChatResponse.java           # 响应DTO
├── enums/
│   └── AiProvider.java             # AI提供商枚举
└── exception/
    └── AiServiceException.java     # 自定义异常
```

## 🧪 测试示例

### cURL测试命令
```bash
# 测试服务是否正常
curl http://localhost:8080/demo/test

# 获取支持的AI提供商
curl http://localhost:8080/demo/providers

# 使用文心一言聊天
curl -X POST http://localhost:8080/demo/qianfan \
  -H "Content-Type: application/json" \
  -d '{"message": "你好，请介绍一下自己"}'

# 使用智谱AI聊天
curl -X POST http://localhost:8080/demo/zhipu \
  -H "Content-Type: application/json" \
  -d '{"message": "请解释什么是机器学习"}'

# 流式聊天测试
curl -X POST http://localhost:8080/demo/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "请写一个Python函数计算斐波那契数列"}'
```

### JavaScript测试
```javascript
// 同步聊天
fetch('http://localhost:8080/demo/qianfan', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({message: '你好'})
}).then(r => r.json()).then(console.log);

// 流式聊天
const response = await fetch('http://localhost:8080/demo/stream', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({message: '介绍一下人工智能'})
});

const reader = response.body.getReader();
while (true) {
  const {done, value} = await reader.read();
  if (done) break;
  console.log(new TextDecoder().decode(value));
}
```

## 🎯 特性

- ✅ **多AI提供商支持**: 支持百度文心一言、智谱AI、OpenAI等
- ✅ **同步/异步聊天**: 支持普通请求和流式响应
- ✅ **统一API接口**: 提供一致的调用接口
- ✅ **错误处理**: 完善的异常处理机制
- ✅ **详细日志**: 完整的请求响应日志
- ✅ **易于扩展**: 可轻松添加新的AI提供商

## 📝 注意事项

1. 确保所有AI服务的API key都正确配置
2. 某些AI服务可能需要网络代理（如OpenAI）
3. 流式响应适合长文本生成，同步响应适合简单问答
4. 建议在生产环境中添加限流和认证机制

## 🔍 扩展开发

如需添加新的AI提供商：

1. 在 `AiProvider` 枚举中添加新提供商
2. 在 `AiModelConfig` 中配置对应的ChatModel
3. 在 `AiChatService` 中添加对应的处理逻辑
4. 在配置文件中添加对应的配置项

享受使用多AI服务的乐趣！🎉