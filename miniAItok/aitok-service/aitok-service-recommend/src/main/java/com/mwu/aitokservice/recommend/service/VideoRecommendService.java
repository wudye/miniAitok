package com.mwu.aitokservice.recommend.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.*;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.domain.VideoPosition;
import com.mwu.aitok.model.video.domain.VideoTag;
import com.mwu.aitok.model.video.enums.PositionFlag;
import com.mwu.aitok.model.video.enums.PublishType;
import com.mwu.aitok.model.video.vo.UserVideoCompilationInfoVO;
import com.mwu.aitok.model.video.vo.VideoVO;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitokservice.recommend.event.VideoRecommendEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mwu.aitok.model.constants.VideoCacheConstants.VIDEO_IMAGES_PREFIX_KEY;
import static com.mwu.aitok.model.constants.VideoCacheConstants.VIDEO_POSITION_PREFIX_KEY;


/**
 * 视频推荐服务
 *
 * @AUTHOR: mwu
 * @DATE: 2024/4/27
 **/
@Slf4j
@Service
public class VideoRecommendService {

    private static final int VIDEO_RECOMMEND_COUNT = 10; // 推荐视频数量
    private static final int PULL_VIDEO_RECOMMEND_THRESHOLDS = 100; // 拉取推荐视频阈值
    private static final int PULL_VIDEO_RECOMMEND_COUNT = 100; // 拉取推荐视频数量

    @Resource
    private RedisService redisService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private VideoRecommendLoadBalancer videoRecommendLoadBalancer;

    //     @DubboReference(retries = 3, mock = "return null")
    @GrpcClient("aitok-video")
    private VideoServiceGrpc.VideoServiceBlockingStub rpcVideoServiceBlockingStub;



    @GrpcClient("aitok-behave")
    private com.mwu.aitok.BehaveServiceGrpc.BehaveServiceBlockingStub rpcBehaveServiceBlockingStub;


    @GrpcClient("aitok-member")
    private com.mwu.aitok.MemberServiceGrpc.MemberServiceBlockingStub rpcMemberServiceBlockingStub;

    @GrpcClient("aitok-social")
    private com.mwu.aitok.SocialServiceGrpc.SocialServiceBlockingStub rpcSocialServiceBlockingStub;



    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private UserTagModalRecommendService userTagModalRecommendService;

    /**
     * 解决异步线程无法访问主线程的ThreadLocal
     */
    private static final ThreadLocal<Long> userIdThreadLocal = new InheritableThreadLocal<>();

    public static void setUserId(Long userId) {
        userIdThreadLocal.set(userId);
    }

    public static Long getUserId() {
        return userIdThreadLocal.get();
    }

    /**
     * 获取推荐视频流
     */
    public List<VideoVO> pullVideoFeed() {
        if (UserContext.hasLogin()) {
            Long userId = UserContext.getUserId();
            String listKey = "recommend:user_recommend_videos:" + userId;
//            List<String> top10Items = lpopItemsFromRedisList(listKey, VIDEO_RECOMMEND_COUNT); // 耗时占了30% 400ms =》优化后 33ms
            List<String> top10Items = lpopItemsFromRedisListOptimized(redisTemplate, listKey, VIDEO_RECOMMEND_COUNT); // 优化后 33ms
            if (sizeOfRedisList(listKey) < PULL_VIDEO_RECOMMEND_THRESHOLDS) {
                // redis list 推荐列表小于 阈值 发送事件补充推荐列表
                applicationEventPublisher.publishEvent(new VideoRecommendEvent(this, userId));
            }

            List<Long> videoIdLongList = top10Items.stream().filter(Objects::nonNull).map(Long::valueOf).toList();
            VideoIdListRequest videoIdListRequest = VideoIdListRequest.newBuilder()
                    .addAllVideoIds(videoIdLongList)
                    .build();

            VideoListResponse videoListResponse = rpcVideoServiceBlockingStub.apiGetVideoListByVideoIds(videoIdListRequest);
            List<Video> videoList = videoListResponse.getVideoListList().stream()
                    .map(videoProto -> BeanCopyUtils.copyBean(videoProto, Video.class))
                    .toList();
            if (CollectionUtils.isEmpty(videoList)) {
                return new ArrayList<>();
            }
            // 过滤空值
            videoList = videoList.stream().filter(Objects::nonNull).collect(Collectors.toList());
            List<VideoVO> videoVOList = BeanCopyUtils.copyBeanList(videoList, VideoVO.class);
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(videoVOList
                    .stream()
                    .map(vo -> packageUserVideoVOAsync(vo, userId))
                    .toArray(CompletableFuture[]::new));
            allFutures.join();
            return videoVOList;
        } else {
            // todo 未登录 用户未登录如何推送
            Long userIdUnLogin = 2L;
            List<String> videoIdsByUserModel = userTagModalRecommendService.getVideoIdsByUserModel(userIdUnLogin);
            List<Long> videoIdLongList = videoIdsByUserModel.stream().filter(Objects::nonNull).map(Long::valueOf).toList();

            VideoIdListRequest videoIdListRequest = VideoIdListRequest.newBuilder()
                    .addAllVideoIds(videoIdLongList)
                    .build();
            VideoListResponse videoListResponse = rpcVideoServiceBlockingStub.apiGetVideoListByVideoIds(videoIdListRequest);
            List<Video> videoList = videoListResponse.getVideoListList().stream()
                    .map(videoProto -> BeanCopyUtils.copyBean(videoProto, Video.class))
                    .toList();
            if (CollectionUtils.isEmpty(videoList)) {
                return new ArrayList<>();
            }
            List<VideoVO> videoVOList = BeanCopyUtils.copyBeanList(videoList, VideoVO.class);
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(videoVOList
                    .stream()
                    .map(vo -> packageUserVideoVOAsync(vo, null))
                    .toArray(CompletableFuture[]::new));
            allFutures.join();
            return videoVOList;
        }
    }

    /**
     * redis list长度
     */
    public Long sizeOfRedisList(String key) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        return listOps.size(key);
    }

    /**
     * redis list lpop
     *
     * @param key
     * @param count
     * @return
     */
    public List<String> lpopItemsFromRedisList(String key, int count) {
        ListOperations<String, String> listOps = redisTemplate.opsForList();
        List<String> items = listOps.range(key, 0, count - 1); // 获取列表中的前20条数据
        for (int i = 0; i < count; i++) {
            listOps.leftPop(key); // 从左侧弹出数据
        }
        return items;
    }

    /**
     * redis list lpop优化
     *
     * @param redisTemplate
     * @param key
     * @param count
     * @return
     */
    public List<String> lpopItemsFromRedisListOptimized(RedisTemplate<String, String> redisTemplate, String key, int count) {
        // 使用Redis Script执行批量LPOP操作
        DefaultRedisScript<List> script = new DefaultRedisScript<>(
                "local result = {}; " +
                        "for i=1," + count + " do " +
                        "  table.insert(result, redis.call('lpop', KEYS[1])); " +
                        "end; " +
                        "return result;",
                List.class
        );

        // 执行Lua脚本并获取结果
        List<String> items = (List<String>) redisTemplate.execute(script, Collections.singletonList(key));

        return items;
    }

    @Async
    public CompletableFuture<Void> packageUserVideoVOAsync(VideoVO videoVO, Long loginUserId) {
        return CompletableFuture.runAsync(() -> packageUserVideoVO(videoVO, loginUserId));
    }

    @Async
    public void packageUserVideoVO(VideoVO videoVO, Long loginUserId) {
        CompletableFuture<Void> behaveDataFuture = packageVideoBehaveDataAsync(videoVO);
        CompletableFuture<Void> memberDataFuture = packageMemberDataAsync(videoVO);
        CompletableFuture<Void> imageDataFuture = packageVideoImageDataAsync(videoVO);
        CompletableFuture<Void> socialDataAsync = packageVideoSocialDataAsync(videoVO, loginUserId);
        CompletableFuture<Void> tagDataAsync = packageVideoTagDataAsync(videoVO);
        CompletableFuture<Void> positionDataAsync = packageVideoPositionDataAsync(videoVO);
        CompletableFuture<Void> videoCompilationDataAsync = packageVideoCompilationDataAsync(videoVO);
        CompletableFuture.allOf(
                behaveDataFuture,
                memberDataFuture,
                imageDataFuture,
                socialDataAsync,
                tagDataAsync,
                positionDataAsync,
                videoCompilationDataAsync
        ).join();
    }

    @Async
    public CompletableFuture<Void> packageVideoBehaveDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoBehaveData(videoVO));
    }

    @Async
    public CompletableFuture<Void> packageMemberDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageMemberData(videoVO));
    }

    @Async
    public CompletableFuture<Void> packageVideoImageDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoImageData(videoVO));
    }

    @Async
    public CompletableFuture<Void> packageVideoSocialDataAsync(VideoVO videoVO, Long loginUserId) {
        return CompletableFuture.runAsync(() -> packageVideoSocialData(videoVO, loginUserId));
    }

    @Async
    public CompletableFuture<Void> packageVideoTagDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoTagData(videoVO));
    }

    @Async
    public CompletableFuture<Void> packageVideoPositionDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoPositionData(videoVO));
    }

    @Async
    public CompletableFuture<Void> packageVideoCompilationDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoCompilationData(videoVO));
    }

    /**
     * 封装视频行为数据
     */
    @Async
    public void packageVideoBehaveData(VideoVO videoVO) {
        // 封装观看量、点赞数、收藏量
//        Integer cacheViewNum = redisService.getCacheMapValue(VideoCacheConstants.VIDEO_VIEW_NUM_MAP_KEY, videoVO.getVideoId());
//        videoVO.setViewNum(StringUtils.isNull(cacheViewNum) ? 0L : cacheViewNum);

        NumRequest numRequest = NumRequest.newBuilder()
                .setVideoId(Long.parseLong(videoVO.getVideoId()))
                .build();

        NumResponse viewNumResponse = rpcBehaveServiceBlockingStub.apiGetVideoLikeNum(numRequest);

        Long likeNum =viewNumResponse.getNum();
        videoVO.setLikeNum(likeNum);

        // 收藏数
        NumResponse favoriteNumResponse = rpcBehaveServiceBlockingStub.apiGetVideoFavoriteNum(numRequest);
        Long favoriteNum = favoriteNumResponse.getNum();
        videoVO.setFavoritesNum(favoriteNum);
        // 评论数
        NumResponse commentNumResponse = rpcBehaveServiceBlockingStub.apiGetVideoCommentNum(numRequest);
        Long commentNum = commentNumResponse.getNum();
        videoVO.setCommentNum( commentNum);
    }

    /**
     * 封装用户数据
     */
    @Async
    public void packageMemberData(VideoVO videoVO) {
        // 封装用户信息
        GetByIdRequest memberRequest = GetByIdRequest.newBuilder()
                .setUserId(videoVO.getUserId())
                .build();
        MemberResponse memberResponse = rpcMemberServiceBlockingStub.getById(memberRequest);
        Member member = BeanCopyUtils.copyBean(memberResponse, Member.class);


        videoVO.setUserNickName(Objects.isNull(member) ? "-" : member.getNickName());
        videoVO.setUserAvatar(Objects.isNull(member) ? null : member.getAvatar());
    }

    /**
     * 封装视频社交数据
     */
    @Async
    public void packageVideoSocialData(VideoVO videoVO, Long loginUserId) {
        if (StringUtils.isNotNull(loginUserId)) {
            // 是否关注、是否点赞、是否收藏
            TwoNumRequest twoNumRequest = TwoNumRequest.newBuilder()
                    .setUserId(loginUserId)
                    .setVideoId(videoVO.getVideoId())
                    .build();

            TrueResponse twoNumResponse = rpcBehaveServiceBlockingStub.apiWeatherLikeVideo(twoNumRequest);

            videoVO.setWeatherLike(twoNumResponse.getResult());

            twoNumResponse = rpcBehaveServiceBlockingStub.apiWeatherFavoriteVideo(twoNumRequest);
            videoVO.setWeatherFavorite(twoNumResponse.getResult());
            if (videoVO.getUserId().equals(loginUserId)) {
                videoVO.setWeatherFollow(true);
            } else {
                FellowRequest fellowRequest = FellowRequest.newBuilder()
                        .setUserId(loginUserId)
                        .setFollowUserId(videoVO.getUserId())
                        .build();
                FellowResponse fellowResponse = rpcSocialServiceBlockingStub.apiWeatherFollow(fellowRequest);
                videoVO.setWeatherFollow(fellowResponse.getSuccess());
            }
        }
    }

    /**
     * 封装视频标签数据
     */
    @Async
    public void packageVideoTagData(VideoVO videoVO) {
        // 封装标签返回
        VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder()
                .setVideoId(Long.parseLong(videoVO.getVideoId()))
                .build();
        VideoTagListResponse videoTagResponse = rpcVideoServiceBlockingStub.queryVideoTagsByVideoId(videoIdRequest);
        List<VideoTag> videoTagList = videoTagResponse.getVideoTagListList().stream()
                .map(videoTagProto -> BeanCopyUtils.copyBean(videoTagProto, VideoTag.class))
                .toList();
        videoVO.setTags(videoTagList.stream().map(VideoTag::getTag).toArray(String[]::new));
    }

    @Autowired
    private ObjectMapper objectMapper;
    /**
     * 封装图文数据
     */
// 修改后的封装图文数据片段
    @Async
    public void packageVideoImageData(VideoVO videoVO) {
        if (videoVO.getPublishType().equals(PublishType.IMAGE.getCode())) {
            String cacheKey = VIDEO_IMAGES_PREFIX_KEY + videoVO.getVideoId();
            Object imgsCacheObject = redisService.getCacheObject(cacheKey);
            if (StringUtils.isNotNull(imgsCacheObject)) {
                String[] images = null;
                try {
                    if (imgsCacheObject instanceof String) {
                        // JSON 字符串 -> String[]
                        images = objectMapper.readValue((String) imgsCacheObject, String[].class);
                    } else if (imgsCacheObject instanceof String[]) {
                        images = (String[]) imgsCacheObject;
                    } else if (imgsCacheObject instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> list = (List<Object>) imgsCacheObject;
                        images = list.stream()
                                .filter(Objects::nonNull)
                                .map(Object::toString)
                                .toArray(String[]::new);
                    }
                } catch (Exception e) {
                    log.warn("解析图片缓存失败 key={} value={} err={}", cacheKey, imgsCacheObject, e.getMessage());
                }
                if (images != null) {
                    videoVO.setImageList(images);
                    return;
                }
            }
            // 缓存未命中或解析失败，走 RPC 重建
            VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder()
                    .setVideoId(Long.parseLong(videoVO.getVideoId()))
                    .build();
            VideoImagesListResponse videoImagesListResponse =
                    rpcVideoServiceBlockingStub.apiGetVideoImagesByVideoId(videoIdRequest);
            List<String> imageList = videoImagesListResponse.getVideoImagesListList().stream()
                    .map(VideoImagesResponse::getImageUrl)
                    .toList();
            String[] imgs = imageList.toArray(new String[0]);
            videoVO.setImageList(imgs);
            try {
                // 统一以 JSON 字符串缓存，兼容跨语言与后续扩展
                redisService.setCacheObject(cacheKey, objectMapper.writeValueAsString(imgs));
                redisService.expire(cacheKey, 1, TimeUnit.DAYS);
            } catch (Exception e) {
                log.warn("写入图片缓存失败 key={} err={}", cacheKey, e.getMessage());
            }
        }
    }

    /**
     * 封装视频定位数据
     */
    @Async
    public void packageVideoPositionData(VideoVO videoVO) {
        // 若是开启定位，封装定位
        if (videoVO.getPositionFlag().equals(PositionFlag.OPEN.getCode())) {
            // 查询redis缓存
            VideoPosition videoPositionCache = redisService.getCacheObject(VIDEO_POSITION_PREFIX_KEY + videoVO.getVideoId());
            if (StringUtils.isNotNull(videoPositionCache)) {
               // TODO videoVO.setPosition(videoPositionCache);
            } else {
                VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder()
                        .setVideoId(Long.parseLong(videoVO.getVideoId()))
                        .build();
                VideoPositionResponse videoPositionResponse = rpcVideoServiceBlockingStub.apiGetVideoPositionByVideoId(videoIdRequest);
                VideoPosition videoPosition = BeanCopyUtils.copyBean(videoPositionResponse, VideoPosition.class);
               //  TODO  videoVO.setPosition(videoPosition);
                // 重建缓存
                redisService.setCacheObject(VIDEO_POSITION_PREFIX_KEY + videoVO.getVideoId(), videoPosition);
                redisService.expire(VIDEO_POSITION_PREFIX_KEY + videoVO.getVideoId(), 1, TimeUnit.DAYS);
            }
        }
    }

    /**
     * 查询视频合集
     */
    @Async
    public void packageVideoCompilationData(VideoVO videoVO) {
        VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder()
                .setVideoId(Long.parseLong(videoVO.getVideoId()))
                .build();
        UserVideoCompilationInfoVOResponse userVideoCompilationInfoVOResponse = rpcVideoServiceBlockingStub.apiGetUserVideoCompilationInfoVO(videoIdRequest);

        UserVideoCompilationInfoVO userVideoCompilationInfoVO = BeanCopyUtils.copyBean(userVideoCompilationInfoVOResponse, UserVideoCompilationInfoVO.class);
        if (Objects.nonNull(userVideoCompilationInfoVO)) {
            videoVO.setUserVideoCompilationInfoVO(userVideoCompilationInfoVO);
        }
    }


}
