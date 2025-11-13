# A full stack project is TikTok + AI 
# Backend:
## Frame design:
- service governance: spring config + eureka
- Api gateway: spring gateway
- services communite: spring cloud openfeign + gRPC
- circuit breaking and degradation: resillence4j
- mointering and visulization: grafana

# core tech stacks
| tech type  |  tech select | version | description |
|---------|---------|------|------|
| bash frame | Spring Boot | 3.4.5 | bash framework|
| microservice | Spring Cloud | 2024.0.1 | microservice |
| RPC | gRPC | 1.75.0 | rpc communicate |
| database | MySQL | 8.x | relational |
| connect pool | HikariCP | 1.2.24 | database connect pool |
| ORM FRAMEWORK | JPA(Hibernate) | 3.5.5 | persistence api |
| cache | Redis | - | distribational cache |
| search engine | Elasticsearch | 8.x | full text search |
| message queue | RabbitMQ | - | async message handle |
| 文件存储 | 阿里云OSS/七牛云 | - | 对象存储服务 |
| video handle | FFmpeg | 3.x | Video transcoding |
| 短信服务 | 阿里云SMS | - | 短信验证码 |
| AI | Spring AI | - | chatbot |
| Tools | Hutool | 5.8.40 | Java tools |
| JSON处理 | FastJSON | 2.0.34 | JSON序列化 |
| Auth | JWT | 0.12.7 | user authenticate |
| containerization | Docker | - |  |


# project structure

##  spring cloud

##  feign + spring config + eureka
    西方也有类似 Dubbo 的技术，主要用于微服务架构下的服务治理和远程调用。常见的有：
    gRPC（Google）：高性能、通用的开源 RPC 框架，支持多语言，广泛用于微服务通信。
    Spring Cloud（Pivotal/VMware）：提供服务注册、发现、负载均衡等微服务治理能力，常与 Netflix OSS 组件结合。
    Apache Thrift（原 Facebook）：跨语言的高效服务开发框架，支持 RPC 通信。
    这些技术在功能和应用场景上与 Dubbo 类似，是西方主流的微服务通信和治理方案。
## she


# original version:
##    backend: spring alibaba cloud + mybatis
##    frontend: vue

