# AI Demo服务

这是一个基于Spring AI的多AI提供商演示服务，支持百度文心一言、智谱AI、OpenAI等多个AI服务。

## 🚀 快速开始

### 1. 构建项目
```bash
mvn clean package
```

### 2. 运行应用
```bash
# 方式1: 使用Maven运行
mvn spring-boot:run

# 方式2: 运行JAR文件
java -jar target/aitok-demo-ai.jar

# 方式3: 指定Profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 访问服务
- 🌐 **应用地址**: http://localhost:8080
- 📖 **API文档**: http://localhost:8080/swagger-ui.html
- 🔍 **健康检查**: http://localhost:8080/actuator/health
- 📊 **监控指标**: http://localhost:8080/actuator/metrics

## 📡 API接口

### 基础测试
```bash
# 测试服务状态
curl http://localhost:8080/api/ai/test

# 查看所有配置的AI服务
curl http://localhost:8080/api/ai/services
```

### AI聊天接口

#### 百度文心一言
```bash
curl -X POST http://localhost:8080/api/ai/chat/qianfan \
  -H "Content-Type: application/json" \
  -d '{"message": "你好，请介绍一下自己"}'
```

#### 智谱AI
```bash
curl -X POST http://localhost:8080/api/ai/chat/zhipu \
  -H "Content-Type: application/json" \
  -d '{"message": "请解释什么是人工智能"}'
```

#### OpenAI
```bash
curl -X POST http://localhost:8080/api/ai/chat/openai \
  -H "Content-Type: application/json" \
  -d '{"message": "Tell me about AI"}'
```

#### 流式聊天
```bash
curl -X POST http://localhost:8080/api/ai/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "请写一首诗"}'
```

## 🛠️ 配置说明

### 已配置的AI服务

1. **百度文心一言** ✅
   - API Key: `x0cuLZ7XsaTCU08vuJWO87Lg`
   - Secret Key: `R9mYF9dl9KASgi5RUq0FQt3wRisSnOcK`
   - Model: `ernie-bot-4`

2. **智谱AI** ✅
   - API Key: `32f84543e54eee31f8d56b2bd6020573.3vh9idLJZ2ZhxDEs`
   - Model: `glm-4`

3. **OpenAI** ✅
   - API Key: `sk-aN6nWn3fILjrgLFT0fC4Aa60B72e4253826c77B29dC94f17`
   - Base URL: `https://api.gptsapi.net`
   - Model: `gpt-3.5-turbo`

4. **阿里通义千问** ⚙️
   - API Key: `sk-7d903764249848cfa912733146da12d1`
   - Model: `qwen-turbo`

5. **月之暗面(KIMI)** ⚙️
   - API Key: `sk-abc` (需要更新)
   - Model: `moonshot-v1-8k`

6. **Azure OpenAI** ⚙️
   - Endpoint: `https://eastusprejade.openai.azure.com`
   - API Key: `xxx` (需要更新)

7. **Ollama** ⚙️
   - URL: `http://127.0.0.1:11434`
   - Model: `llama3`

8. **Stability AI** ⚙️
   - API Key: `sk-e53UqbboF8QJCscYvzJscJxJXoFcFg4iJjl1oqgE7baJETmx`

9. **Minimax** ⚙️
   - API Key: `xxxx` (需要更新)

### 向量数据库

- **Redis**: `127.0.0.1:6379`
- **Qdrant**: `127.0.0.1:6334`
- **Milvus**: `127.0.0.1:19530`

## 📁 项目结构

```
demo-ai/
├── src/main/java/com/mwu/demo/
│   ├── DemoAiApplication.java     # 启动类
│   └── controller/
│       └── AiTestController.java # 测试控制器
├── src/main/resources/
│   └── application.yml           # 配置文件
├── pom.xml                        # Maven配置
└── README.md                      # 说明文档
```

## 🔧 开发配置

### Profiles

- **dev**: 开发环境，日志级别DEBUG，温度参数0.8
- **prod**: 生产环境，日志级别WARN，温度参数0.3
- **默认**: 基础配置

### 监控端点

```bash
# 健康检查
curl http://localhost:8080/actuator/health

# 应用信息
curl http://localhost:8080/actuator/info

# 所有指标
curl http://localhost:8080/actuator/metrics

# Prometheus格式指标
curl http://localhost:8080/actuator/prometheus
```

## 🧪 测试

### 单元测试
```bash
mvn test
```

### 集成测试
```bash
mvn verify
```

## 📝 注意事项

1. **API Key安全**: 生产环境中请使用环境变量或配置中心管理API密钥
2. **网络访问**: 某些AI服务可能需要代理访问（如OpenAI）
3. **资源限制**: 注意AI服务的调用频率和token限制
4. **错误处理**: 应用包含基础的错误处理，生产环境需要更完善的异常处理

## 🔄 扩展开发

### 添加新的AI提供商

1. 在 `application.yml` 中添加对应配置
2. 添加相应的Spring AI starter依赖
3. 在控制器中添加新的接口

### 自定义配置

可以通过以下方式自定义配置：

```yaml
spring:
  ai:
    qianfan:
      chat:
        options:
          temperature: 0.7
          max-tokens: 2000
          model: ernie-bot-4
```

## 🐛 故障排除

### 常见问题

1. **依赖冲突**: 检查Spring AI版本兼容性
2. **API Key错误**: 确认API密钥正确且有效
3. **网络连接**: 检查网络连接和代理设置
4. **配置错误**: 检查 `application.yml` 配置格式

### 日志查看

```bash
# 开发环境详细日志
logging:
  level:
    org.springframework.ai: TRACE

# 生产环境精简日志
logging:
  level:
    org.springframework.ai: WARN
```

## 📞 支持

如有问题，请查看：
1. Spring AI官方文档: https://spring.io/projects/spring-ai
2. 各AI服务提供商的API文档
3. 应用日志输出

享受使用AI服务的乐趣！🎉