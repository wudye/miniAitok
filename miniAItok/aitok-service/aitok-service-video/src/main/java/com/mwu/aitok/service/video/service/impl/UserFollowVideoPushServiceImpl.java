package com.mwu.aitok.service.video.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitiokcoomon.core.utils.date.DateUtils;
import com.mwu.aitok.service.video.service.UserFollowVideoPushService;
import com.mwu.aitokcommon.cache.service.RedisService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.mwu.aitok.model.constants.VideoConstants.IN_FOLLOW;
import static com.mwu.aitok.model.constants.VideoConstants.OUT_FOLLOW;

@Service
public class UserFollowVideoPushServiceImpl implements UserFollowVideoPushService {

    @Resource
    private RedisService redisService;

    /*
    @Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        // StringRedisTemplate 内部已设置 StringRedisSerializer，直接返回即可
        return new StringRedisTemplate(factory);
    }
}

    RedisTemplate 更通用，常用 StringRedisSerializer 做 key 序列化、JSON（如 GenericJackson2JsonRedisSerializer）做 value 序列化，用于存储对象/复杂类型，需要手动配置序列化器。

     */
    @Resource
    private RedisTemplate redisTemplate;

    /*

    StringRedisTemplate 是 RedisTemplate<String, String> 的专用子类，默认用 StringRedisSerializer 对 key/value 做序列化，适合纯字符串场景。
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 推入发件箱
     *
     * @param userId  发件箱用户id
     * @param videoId 视频id
     * @param time    视频发布时间戳
     */
    @Override
    public void pusOutBoxFeed(Long userId, String videoId, Long time) {

        redisService.setCacheZSet(OUT_FOLLOW + userId, videoId, time);

    }

    /**
     * 推入收件箱
     *
     * @param userId  用户id
     * @param videoId 视频id
     * @param time    视频发布时间戳
     */
    @Override
    public void pushInBoxFeed(Long userId, String videoId, Long time) {
        // todo 主动推模式暂时不用（当粉丝数据过多或者僵尸粉丝带来过大性能开销）
    }

    /**
     * 删除发件箱
     * 当前用户删除视频时 调用->删除当前用户的发件箱中视频以及粉丝下的收件箱
     *
     * @param userId  当前用户
     * @param fans    粉丝ids
     * @param videoId 视频id 需要删除的
     */
    @Override
    public void deleteOutBoxFeed(Long userId, List<Long> fans, String videoId) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long fan : fans) {
                //。zRem 是 Redis 的有序集合（ZSet）命令，用于从集合中移除指定的成员
                connection.zRem((IN_FOLLOW + fan).getBytes(), videoId.getBytes());
            }
            connection.zRem((OUT_FOLLOW + userId).getBytes(), videoId.getBytes());
            return null;
        });
    }


    /**
     * 删除收件箱
     * 当前用户取关用户时调用->删除自己收件箱中的videoIds
     *
     * @param userId
     * @param videoIds 关注人发的视频id
     */
    @Override
    public void deleteInBoxFeed(Long userId, List<String> videoIds) {
        redisTemplate.opsForZSet().remove(IN_FOLLOW + userId, videoIds.toArray());
    }

    /**
     * 初始化关注流->拉取模式 with TTL
     *
     * @param userId
     * @param followIds
     */
    @Override
    public void initFollowVideoFeed(Long userId, List<Long> followIds) {

        Date curDate = DateUtils.getNowDate();
        //DateUtils.addDays(curDate, -365) 的含义是从当前日期向前减去 365 天（即一年），返回的结果是目标日期。
        Date limitDate = DateUtils.addDays(curDate, -365);
//zRangeWithScores 是 Redis 的 ZSet 操作，用于获取指定范围内的元素及其分数。这里的范围参数是 -1, -1，表示只获取分数最高的一个元素（即 ZSet 中的最后一个元素
        // 返回值：
        //返回值是一个 Set 集合，包含 ZSetOperations.TypedTuple<String> 类型的元素。每个 TypedTuple 包含两个部分：元素值（String 类型）和对应的分数（Double 类型）。这使得开发者可以同时获取元素和分数信息。
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisService.zRangeWithScores(IN_FOLLOW + userId, -1, -1);
        if (!CollectionUtils.isEmpty(typedTuples)) {
            Double oldTime = typedTuples.iterator().next().getScore();
            init(userId, oldTime.longValue(), new Date().getTime(), followIds);
        } else {
            init(userId, limitDate.getTime(), curDate.getTime(), followIds);
        }
    }

    /*
    @SneakyThrows 是 Lombok 提供的注解，用来在编译时绕过 Java 的受检异常（checked exceptions）检查。它的效果是：不需要在方法签名上声明 throws，也不需要在方法体里显式捕获某些检查型异常；Lombok 在编译时修改字节码，使异常以“sneaky throw” 的方式抛出，调用方不被强制捕获或声明。
作用与注意点：
方便：减少 try/catch 或方法签名的样板代码，常见于测试或 lambda 场景（lambda 签名不允许声明受检异常）。
风险：隐藏了方法会抛出哪些检查型异常，可能让调用者不易发现和处理异常，降低可读性和 API 明确性。
     */
    @SneakyThrows
    public void init(Long userId, Long min, Long max, Collection<Long> followIds) {
        // 查看关注人的发件箱
        List<Set<DefaultTypedTuple>> result = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long followId : followIds) {
                // 0 和 20：分页参数，表示从结果集中获取前 20 个元素。
                connection.zRevRangeByScoreWithScores((OUT_FOLLOW + followId).getBytes(), min, max, 0, 20);
            }
            return null;
        });
        final ObjectMapper objectMapper = new ObjectMapper();
        // 放入用户收件箱
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Set<DefaultTypedTuple> tuples : result) {
                if (!ObjectUtils.isEmpty(tuples)) {
                    for (DefaultTypedTuple tuple : tuples) {
                        String value = (String) tuple.getValue();
                        byte[] key = (IN_FOLLOW + userId).getBytes();
                        try {
                            connection.zAdd(key, tuple.getScore(), objectMapper.writeValueAsBytes(value));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        // 过期时间一年
                        connection.expire(key, 365 * 24 * 60 * 60L);
                    }
                }
            }
            return null;
        });

    }

}
