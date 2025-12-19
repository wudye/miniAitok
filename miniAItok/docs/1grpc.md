# grpc can be used to communicate between microservices with high performance and low latency.
    it can invoke methods on a remote server as if they were local methods.
    So it can let us build distributed systems more easily.
    In this project, grpc is an optional module, you can choose to use it or not.
    It's logic:
    • Define a service that specifies the methods that can be invoked remotely (including parameters and return types).
    • Implement that interface on the server and run a gRPC server to handle client calls.
    • On the client side, have a stub that allows calling the remote service in the same way as calling a local object
    • Use protocol buffers (protobuf) to serialize and deserialize messages between client and server.



简短结论：gRPC 是面向高性能、强类型 RPC（基于 HTTP/2 + Protobuf）的框架；OpenFeign 是面向 REST/HTTP 的声明式客户端（通常配合 JSON）。选哪个看需求：性能/双向流/严格契约用 gRPC；兼容 HTTP/浏览器、人可读 API、快速集成 Spring Cloud 用 OpenFeign。
主要比较（要点）：
协议与传输
gRPC：基于 HTTP/2，支持多路复用、低延迟、双向流。
OpenFeign：基于 HTTP/1.1/REST（JSON/HTTP），简单通用，容易调试。
序列化与契约
gRPC：Protobuf（强类型、紧凑、需要生成代码）。
Feign：通常 JSON（灵活、无需生成代码，契约松散）。
性能与延迟
gRPC：更高吞吐、低延迟、节省带宽。
Feign：比 gRPC 稍慢，开销更大（JSON 文本解析）。
流式与双向通信
gRPC：原生支持单向/服务器流/客户端流/双向流。
Feign：可用 HTTP 流或 SSE，但不如 gRPC 原生且复杂。
互操作性与可见性
gRPC：跨语言强；浏览器直接支持有限（需 gRPC-Web）。
Feign：与 REST 生态兼容，容易被浏览器/外部系统调用。
开发体验与生态（在 Spring 里）
gRPC：需要 proto、生成代码、starter/support 存在但比 REST 稍复杂。
Feign：与 Spring Cloud/Netflix 集成顺滑，注解式调用，开发速度快。
服务发现、负载均衡与重试
两者都能配合服务发现/负载均衡，但 gRPC 在长连接场景下需要注意 name resolver/health-check 的配置。
Feign 依赖 HTTP client（Ribbon/Feign client/Resilience4j）实现重试与熔断更常见。
安全与运维
gRPC：支持 TLS、元数据认证，运维需处理 HTTP/2。
Feign：使用常规 HTTP/TLS，更易于调试/网关接入。
何时选择（简明规则）：
选 gRPC：内部微服务、高 QPS、需要低延迟或流式通信且可以接受 Proto 代码生成。
选 OpenFeign：需要对外提供或消费 REST/JSON API、与浏览器/第三方系统兼容性高、开发速度优先。
实例场景（一句话）：
实时音视频/大规模微服务通信：gRPC；
公共 HTTP API、移动/网页前端对接：Feign/REST。

1. 定义 gRPC 服务的 Service API，以及作为 Service API 方法的方法参数和返回类型的 Message。
proto 目录下，创建 UserService.proto 文件，定义用户服务的 Message 和 Service
   <!-- 引入 gRPC Protobuf 依赖，因为使用它作为序列化库 -->
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
            <version>${io.grpc.version}</version>
        </dependency>
        <!-- 引入 gRPC Stub 依赖，因为使用它作为 gRPC 客户端库 -->
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
            <version>${io.grpc.version}</version>
        </dependency>
    </dependencies>

2. 义的 Service API，基于 gRPC Server 实现用户服务。
<!-- 引入 gRPC Netty 依赖，因为使用它作为网络库 -->
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty</artifactId>
            <version>${io.grpc.version}</version>
        </dependency>
3. 创建 UserServiceGrpcImpl 类，继承 UserServiceGrpc.UserServiceImplBase 类，实现用户服务的逻辑。代码如下：
   每个方法的第一个参数，是具体请求。
   每个方法的第二个参数，通过它进行响应。
   UserServiceGrpc.UserServiceImplBase 是 protobuf 文件生成的 Service 的抽象实现类