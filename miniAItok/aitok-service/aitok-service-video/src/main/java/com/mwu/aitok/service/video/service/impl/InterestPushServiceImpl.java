package com.mwu.aitok.service.video.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.domain.VideoTag;
import com.mwu.aitok.model.video.vo.UserModel;
import com.mwu.aitok.model.video.vo.UserModelField;
import com.mwu.aitok.service.video.service.InterestPushService;
import com.mwu.aitok.service.video.service.VideoTagService;
import com.mwu.aitokcommon.cache.service.RedisService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mwu.aitok.service.video.constants.InterestPushConstant.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestPushServiceImpl implements InterestPushService {

    /*
    RedisService 和 RedisTemplate 的主要区别在于它们的抽象层次和用途：
RedisTemplate:
是 Spring Data Redis 提供的核心类，用于直接与 Redis 交互。
提供了丰富的操作方法（如 opsForValue、opsForSet、opsForHash 等），支持对 Redis 的各种数据结构（String、Set、Hash 等）进行操作。
需要开发者手动处理 Redis 的操作逻辑，适合更底层的操作。
RedisService:
是项目中自定义的服务类，通常基于 RedisTemplate 封装。
提供更高层次的抽象，隐藏了底层的 Redis 操作细节。
通过封装常用的 Redis 操作（如 setCacheMap、getCacheMap 等），简化了代码逻辑，提高了可维护性。
总结:
RedisTemplate 是底层工具，直接操作 Redis。
RedisService 是上层封装，提供更简化的接口，便于业务逻辑调用。
     */

    private final RedisService redisService;
    @Resource
    private VideoTagService videoTagService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 将标签对应的视频集合存入redis的set
     */
    @Override
    public void cacheVideoToTagRedis(String videoId, List<Long> tagsIds) {

        tagsIds.forEach(tagId -> {
            redisTemplate.opsForSet().add(VIDEO_TAG_VIDEOS_CACHE_KEY_PREFIX + tagId.toString(), videoId);
        });
    }

    @Override
    public void cacheVideoToCategoryRedis(String videoId, List<Long> categoryIds) {

        categoryIds.forEach(id -> {
            redisTemplate.opsForSet().add(VIDEO_CATEGORY_VIDEOS_CACHE_KEY_PREFIX + id.toString(), videoId);
        });
    }

    /*
    这段代码使用了 Redis 的管道（Pipeline）功能，通过 redisTemplate.executePipelined 方法批量执行删除操作。它的作用是从多个 Redis 的 Set 集合中移除指定的视频 ID。
工作原理
开启管道：executePipelined 方法开启 Redis 管道，允许在一次网络请求中批量执行多个命令。
批量删除：通过 connection.sRem 方法，从每个以 VIDEO_TAG_VIDEOS_CACHE_KEY_PREFIX + tagId 为键的 Set 集合中移除 videoId。
返回结果：由于管道操作的结果会被 RedisTemplate 自动收集，这里返回 null，表示不需要手动处理结果。
假设：
VIDEO_TAG_VIDEOS_CACHE_KEY_PREFIX 为 "video:tag:"。
tagsIds 为 [101L, 102L, 103L]。
videoId 为 "video123"。
执行后，Redis 中的以下键对应的 Set 集合将移除 "video123"：
video:tag:101
video:tag:102
video:tag:103
     */
    @Override
    public void deleteVideoFromTagRedis(String videoId, List<Long> tagsIds) {

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long tagId : tagsIds) {
                connection.sRem((VIDEO_TAG_VIDEOS_CACHE_KEY_PREFIX + tagId).getBytes(), videoId.getBytes());
            }
            return null;
        });
    }

    @Override
    public void deleteVideoFromCategoryRedis(Video video, List<Long> categoryIds) {
        categoryIds.forEach(id -> {
            redisTemplate.opsForSet().remove(VIDEO_CATEGORY_VIDEOS_CACHE_KEY_PREFIX + id, video.getId());
        });
    }

    /**
     * 根据分类推送视频
     *
     * @param categoryId
     * @return
     */
    @Override
    public Collection<String> listVideoIdByCategoryId(Long categoryId) {
        // 随机推送10个
        List<Object> list = redisTemplate.opsForSet().randomMembers(VIDEO_CATEGORY_VIDEOS_CACHE_KEY_PREFIX + categoryId.toString(), 10);
        if (list.isEmpty() || StringUtils.isNull(list)) {
            return null;
        }
        // 可能会有null
        HashSet<String> result = new HashSet<>();
        for (Object aLong : list) {
            if (aLong != null) {
                result.add(aLong.toString());
            }
        }
        // todo 不足10条补足10条
        return result;
    }

    /*
        假设我们有一个用户 ID 为 12345，并且该用户的兴趣标签 ID 列表为 [101L, 102L, 103L]。我们调用 initUserModel 方法，将这些标签初始化到 Redis 缓存中。

        缓存的键为 VIDEO_MEMBER_MODEL_CACHE_KEY_PREFIX + userId，值为一个 Map<String, Double>，其中键为标签 ID，值为概率。
        用户兴趣模型：{101=33.333333333333336, 102=33.333333333333336, 103=33.333333333333336}
        HashMap 在单线程环境下使用是安全的。在 InterestPushServiceImpl 的 initUserModel 方法中，HashMap 仅在方法内部使用，没有多线程访问的场景，因此不会引发线程安全问题。
    如果需要在多线程环境下使用线程安全的 Map，可以考虑使用 ConcurrentHashMap 或通过 Collections.synchronizedMap 包装 HashMap。
         */
    @Override
    public void initUserModel(Long userId, List<Long> tagIds) {
        String key = VIDEO_MEMBER_MODEL_CACHE_KEY_PREFIX + userId;
        Map<String, Double> modelMap = new HashMap<>();

        if (!ObjectUtils.isEmpty(tagIds)) {
            int size = tagIds.size();
            double probability = (double)100 / size;
            for (Long tagId : tagIds) {
                modelMap.put(tagId.toString(), probability);
            }
        }
        redisService.setCacheMap(key, modelMap);
    }

    /**
     * 更新用户模型
     *
     * @param userModel
     */
    @Override
    public void updateUserModel(UserModel userModel) {

        log.debug("userModel:{}", userModel);
        Long userId = userModel.getUserId();
        if (userId != null) {
            List<UserModelField> models = userModel.getModels();
            // 获取用户模型
            String key = VIDEO_MEMBER_MODEL_CACHE_KEY_PREFIX + userId;
            // 原始模型
            Map<Object, Object> modelMap = redisTemplate.opsForHash().entries(key);
            log.debug("modelMap:{}", modelMap);
            if (CollectionUtils.isEmpty(modelMap)) {
                modelMap = new HashMap<>();
            }
            for (UserModelField model : models) {
                // 修改用户模型
                if (modelMap.containsKey(model.getTagId().toString())) {
                    // 源标签存在
                    modelMap.put(model.getTagId().toString(), (Double.parseDouble(modelMap.get(model.getTagId().toString()).toString()) + model.getScore()));
                } else {
                    modelMap.put(model.getTagId().toString(), model.getScore());
                }
            }

            // 模型归一化
            double sum = 0;
            for (Object value : modelMap.values()) {
                sum += Double.parseDouble(value.toString());
            }
            for (Object o : modelMap.keySet()) {
                modelMap.put(o.toString(), (Double.parseDouble(modelMap.get(o.toString()).toString()) * 100) / sum);
                // ConcurrentModificationException
//                Object v = modelMap.get(o.toString());
//                if (v == null || Double.parseDouble(modelMap.get(o.toString()).toString()) < 1.00) {
//                    modelMap.remove(o.toString());
//                }
            }
            // 更新用户模型
            log.debug("modelMap new:{}", modelMap);
            redisTemplate.opsForHash().putAll(key, modelMap);
        }


    }

    /**
     * 初始化概率数组 -> 保存的元素是标签id
     * todo 优化->模型归一化，防止概率小于1的无推荐结果
     */
    public String[] initProbabilityArray(Map<String, Double> modelMap) {
        // key: 标签id  value：概率
        Map<String, Integer> probabilityMap = new HashMap<>();
        int size = modelMap.size();
        // field个数
        /*

        AtomicInteger 是 Java 中 java.util.concurrent.atomic 包提供的一个类，用于在多线程环境下以原子方式更新整数值。它通过底层的 CAS（Compare-And-Swap）操作实现线程安全，避免了使用同步锁的开销。
主要特点：
线程安全：所有操作（如自增、自减、设置值等）都是原子性的。
高效：相比使用 synchronized 或 Lock，性能更高。
非阻塞：基于 CAS 操作，不会引起线程阻塞。
         */
        AtomicInteger num = new AtomicInteger(0);
        modelMap.forEach((k, v) -> {
            // 标签的概率 防止结果为0,每个同等加上标签数
            int probability = (v.intValue() + size) / size;
            probabilityMap.put(k, probability);
            num.getAndAdd(probability);
        });
        // 返回结果初始化
        String[] probabilityArray = new String[num.get()];
        AtomicInteger index = new AtomicInteger(0);
        // 遍历probabilityMap，将每个tagId及其概率p存入probabilityArray中
        probabilityMap.forEach((tagId, p) -> {
            int i = index.get();
            int limit = i + p;
            while (i < limit) {
                probabilityArray[i++] = tagId;
            }
            index.set(limit);
        });
        return probabilityArray;
    }


    public List<Long> random10TagIdsFromUserModel(Member member) {
        Long userId = member.getUserId();
        // 从模型中拿概率 获取hashKey对应的所有概率键值
        Map<String, Double> modelMap = redisService.getCacheMap(VIDEO_MEMBER_MODEL_CACHE_KEY_PREFIX + userId.toString());
        // 标签ids数组 【1，1，1，2，2，66，7，7，7，7】
        String[] probabilityArray = initProbabilityArray(modelMap);
        // 获取视频
        final Random random = new Random();
        // 取出指定量的标签
        List<Long> tagIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String tagId = probabilityArray[random.nextInt(probabilityArray.length)];
            tagIds.add(Long.parseLong(tagId));
        }
        return tagIds;
    }

    /**
     * 从用户模型的标签中随机视频ids
     *
     * @param tagIds
     * @return
     */
    public Set<String> completeVideoIdsFromTagIds(Set<String> videoIds, List<Long> tagIds) {
        if (videoIds.size() >= 10) {
            return videoIds;
        }
        List<String> list = redisService.pipeline(connection -> {
            for (Long tagId : tagIds) {
                String key = VIDEO_TAG_VIDEOS_CACHE_KEY_PREFIX + tagId;
                log.debug("key:{}", key);
                byte[] bytes = connection.sRandMember(Objects.requireNonNull(redisTemplate.getStringSerializer().serialize(key)));
                if (bytes != null) {
                    return redisTemplate.getStringSerializer().deserialize(bytes);
                }
            }
            return null;
        });
        // 获取到的videoIds去重
        Set<String> collect = list.stream().filter(StringUtils::isNotNull).map(Object::toString).collect(Collectors.toSet());
        videoIds.addAll(collect);
        return videoIds;
    }

    public String randomVideoIdFromTag(Long tagId) {
        String key = VIDEO_TAG_VIDEOS_CACHE_KEY_PREFIX + tagId;
        return redisTemplate.opsForSet().randomMember(key).toString();
    }
    @Override
    public Collection<String> getVideoIdsByUserModel(Member member) {

        // todo 根据member查询其兴趣模型是否为空，为空则创建模型

        Map<String, Double> modelMap = redisService.getCacheMap(VIDEO_MEMBER_MODEL_CACHE_KEY_PREFIX + member.getUserId().toString());

        if (StringUtils.isEmpty(modelMap) || modelMap.isEmpty()) {
            initUserModel(member.getUserId(), videoTagService.random10VideoTags().stream().map(VideoTag::getTagId).collect(Collectors.toList()));

        }
        Set<String> videoIds = new HashSet<>(10);

        List<String> list = redisService.pipeline(connection -> {
            for (Long tagId : random10TagIdsFromUserModel(member)) {
                String key = VIDEO_TAG_VIDEOS_CACHE_KEY_PREFIX + tagId;
                log.debug("key:{}", key);
                byte[] bytes = connection.sRandMember(redisTemplate.getStringSerializer().serialize(key));
                if (bytes != null) {
                    return redisTemplate.getStringSerializer().deserialize(bytes);
                }
            }
            return null;
        });

        // 获取到的videoIds去重
        Set<String> setVideoIds = list.stream().filter(StringUtils::isNotNull).map(Object::toString).collect(Collectors.toSet());
        Set<String> videoSetIds = completeVideoIdsFromTagIds(setVideoIds, random10TagIdsFromUserModel(member));
        log.debug("videoSetIds:{}", videoSetIds);


        // 根据视频观看历史去重
//            List<String> simpIds = redisService.pipeline(connection -> {
//                for (String id : videoSetIds) {
//                    String key = VIDEO_VIEW_HISTORY_CACHE_KEY_PREFIX + id + ":" + userId;
//                    byte[] bytes = connection.get(redisTemplate.getStringSerializer().serialize(key));
//                    if (bytes != null) {
//                        return redisTemplate.getStringSerializer().deserialize(bytes);
//                    }
//                }
//                return null;
//            });
//            simpIds = simpIds.stream().filter(StringUtils::isNotNull).collect(Collectors.toList());
//
//            // todo 根据已筛选去重
//
//            if (!ObjectUtils.isEmpty(simpIds)) {
//                for (Object simpId : simpIds) {
//                    String l = simpId.toString();
//                    if (videoSetIds.contains(l)) {
//                        videoSetIds.remove(l);
//                    }
//                }
//            }

        videoIds.addAll(videoSetIds);
        int videoIdsSize = videoIds.size();
        log.debug("videoIds size:{}", videoIdsSize);
        // todo 不够10条数据就随机取标签补全10条，男生推美女，女生推帅哥 o.0

        // 随机补全视频id,根据性别: 男：美女(10) 女：帅哥(1) todo 或者递归再次随机根据模型筛选出视频
        if (videoIdsSize < 10) {
            String sex = member.getSex();
            int requestNum = 10 - videoIdsSize;
            log.debug("requestNum:{}", requestNum);
            for (int i = 0; i < requestNum; i++) {
                String videoId = randomVideoIdFromTag("1".equals(sex) ? 20L : 1L);
                log.debug("add videoId:{}", videoId);
                videoIds.add(videoId);
            }
        }
        return videoIds;


    }

    @Override
    public Collection<String> getVideoIdsByTagIds(List<Long> tagIds) {

        List<String> tagKeys = new ArrayList<>();
        for (Long tagId : tagIds) {
            tagKeys.add("video:tag:videos" + tagId);
        }
        //return this.sRandom(tagKeys).stream().map(Object::toString).collect(Collectors.toList());

        List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String key : tagKeys) {
                connection.sRandMember(key.getBytes());
            }
            return null;
        });
        Set<Object> set1 =  list.stream().filter(StringUtils::isNotNull).collect(Collectors.toSet());
        return set1.stream().map(Object::toString).collect(Collectors.toList());

    }
}
