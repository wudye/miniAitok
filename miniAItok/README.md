# should be spring cloud microservice project
##  spring cloud config, gateway, eureka,  feign, grpc(optional), 
     resilence4j/circuit breaker, redis/ratelimite, kafka, mysql, minio, 
     Elasticsearch, zipkin/sleuth(optional), java email, spring security,
     redis/cache, spring AI, test(junit5 and postman), docker ...
 
# business part
## video recommendation algorithm 
    bring users to use it frequently
    follow,  like, comment, share (community to build)
## ai agent
    chat 
    generate image
    generate video

# rules
    clear structure
    docker(docker-compose, config)
    docs(markdown)
    config,eureka,feign,gateway,service, grpc(optional), common, tools, starter, modael

# common module(public module)
## common-core
## common-cache
1. annotation(DoubleCache) + asject(DubboCacheAspect) to do Multi-Level Cache Architecture(caffeine + redis + mysql)
2. request→ Caffeine（L1） → Redis（L2） → database(mysql)
    strategy:
   read order: Caffeine → Redis → Database
   write order: write to Redis and Caffeine
   update: redis and caffeine
   delete: Redis and Caffeine
   distributed lock with redisson build by annotation(RedissonLock) + aop(RedissonLockAspect)
3. redission ratelimit
   @RateLimiter 注解
        ↓
   RateLimiterAspect（AOP切面）
        ↓
   RateLimiterRedisDAO（数据访问层）
        ↓
   Redisson + Redis（分布式存储）
## common-ai
# video module
    creator module  integrate to video module, multipart upload for video
    redis Anti-Repeat Submit annotation(VideoRepeatSubmit) + aop(VideoRepeatSubmitAspect)
    

# notice moduele, websocket
    client connect → /websocket/123
                ↓
    WebSocketServer.onOpen() → sessionPool.put(123, session)
                ↓
    Controller api → WebSocketServer.sendMessage(123, "消息")
                ↓
    by session sends message to client
                ↓
    onMessage to handle the client message

请求 → [Rate Limiter] → [Anti-Repeat Submit] → [Retry] → 业务逻辑
(入口层)         (业务层)              (服务层)