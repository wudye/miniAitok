# controller user management
    register (username, password,confirmPassword) -> ok
        -> rabbitmq  to send useId to behave service to create the userFavorite
    login (username, password) -> (accessToken, refreshToken)
    logout delete the refreshToken from the token database -> ok
    refreshToken (refreshToken) -> (newAccessToken, newRefreshToken)
    update (Member) -> delete the cache in redis -> update
    userId ->  only use search one time by userId then save to the redis cache -> Member -> do create  MemberInfo
    userinfo> 
    

# reids config for serialize and deserialize
    use json serialize for redis cache
    use json deserialize for redis cache
    redis config -> global ObjectMapper -> redisTemplate 
    for time config -> with jaskson time module -> LocalDateTime serialize and deserialize