# member service
1. register
    1.0 (choose) for register and update content to do senetive check
    1.1 create new in member , member_info table  
    1.2 rabbitmq to behave module to create new in user_favorite 
2. login: with accessToken and refreshToken (with httpOnly put the refreshToken in cookie)
    2.1 for multiple device login, useId:deviceId (can add ip)
    2.2 redis cache the accessToken and refreshToken, save and delete by login and update
    2.3 refreshToken created by randonUUID or jwt
    2.4 accessToken created by jwt
    2.5 with RSAKey replace jwt secret 
    2.6 ratelimit (userId + deviceId + ip) to avoid multiple try(ratelimite annotaion with aop)
3. update:
    redis delete and save
4. get user info
    first check in redis, no check in database and save in redis
    redis serialise
5. uploadImage to minio
    return the minio save url

# video service (integrate the creator module), video side
1. upload video (multipartFile to minio)
    1.1 set the video tags(no more than five)
    1.2 save the video and taginfo to database and redis
other crud
    for multiple services or database with CompletableFuture (Executor )
    use redis and caffeine to cache the data
    with AntiRepeatSubmit annotaion
2. recommend videos to users(after the user login, the system sends) collect the user info to database
    2.1 according  to the user Tags(set by user)
    2.2 according  to the user watch history(record in database)
    2.3 according  to the user follow(record in database)
    2.4 according  to the user favorite(record in database)
    2.5 according  to the user like(record in database)
    2.6 according  to the user comment(record in database)
    2.7 according  to the user collection(record in database)
    2.8 according  to the user location(record in database)
    2.9 according  to the watch videos location(record in database)
    3.0 according  to the watched number to recommend the top list 10 or random
    3.0 recommand the top list 10 or random

3. create a series (buy user)
    allow use create series, save the series info to database

4. record the video positon and other info to database

5. according to the video tags putting videoId to database

6. according to the videos follow and like putting videoId to database


# social service
1. follow and unfollow
2. check the follow status by others
3. check the followers and following by others
4. check the follower and following by myself

# search service
    according the watched history to give the video hot value
    Elasticsearch and redis cache

# recommend service
    create recommend module according to the collected info

# notice service
    handle the notice like follow, like, upload messagee with websocket

# behave service,  user side
1. set favorite videos
2. set user comment
3. set user note
4. set user behave (like, favorite, comment, note)

# ai service(in oneall project)

# common-cache module
1. caffeine, redis, database multi cache architecture
2. distributed lock with redission
3. redission ratelimte
4. redis ratelimite(in video service)

# common-core module(in oneall project)