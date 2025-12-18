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


    
# creator module, multipart upload for video
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
                