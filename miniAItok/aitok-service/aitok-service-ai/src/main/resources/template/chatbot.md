curl -X POST "http://localhost:8080/v1/chat/stream" \
-H "Content-Type: application/json" \
-d '{"conversationId":1,"message":"你好，请介绍一下自己","useContext":true}' \
--no-buffer