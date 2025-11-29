package com.mwu.aitok.service.video.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import com.mwu.aitiokcoomon.core.compont.SnowFlake;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitiokcoomon.core.utils.audit.SensitiveWordUtil;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.date.DateUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.*;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitok.model.common.enums.DelFlagEnum;
import com.mwu.aitok.model.constants.VideoCacheConstants;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.video.domain.*;
import com.mwu.aitok.model.video.dto.UpdateVideoDTO;
import com.mwu.aitok.model.video.dto.VideoFeedDTO;
import com.mwu.aitok.model.video.dto.VideoPageDto;
import com.mwu.aitok.model.video.dto.VideoPublishDto;
import com.mwu.aitok.model.video.enums.PositionFlag;
import com.mwu.aitok.model.video.enums.PublishType;
import com.mwu.aitok.model.video.vo.*;
import com.mwu.aitok.model.video.vo.app.MyVideoVO;
import com.mwu.aitok.model.video.vo.app.VideoInfoVO;
import com.mwu.aitok.model.video.vo.app.VideoRecommendVO;
import com.mwu.aitok.service.video.constants.HotVideoConstants;
import com.mwu.aitok.service.video.constants.QiniuVideoOssConstants;
import com.mwu.aitok.service.video.constants.VideoConstants;
import com.mwu.aitok.service.video.domain.MediaVideoInfo;
import com.mwu.aitok.service.video.repository.*;
import com.mwu.aitok.service.video.service.*;
import com.mwu.aitok.service.video.service.cache.VideoRedisBatchCache;
import com.mwu.aitokcommon.cache.annotations.RedissonLock;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitokstarter.file.service.MinioService;
import com.mwu.aitokstarter.video.service.FfmpegVideoService;
import com.mwu.aitolk.feign.behave.RemoteBehaveService;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.units.qual.N;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.info.MultimediaInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.mwu.aitok.model.common.enums.HttpCodeEnum.SENSITIVEWORD_ERROR;
import static com.mwu.aitok.model.constants.VideoCacheConstants.*;
import static com.mwu.aitok.model.video.mq.VideoDelayedQueueConstant.*;
import static com.mwu.aitok.model.video.mq.VideoDirectExchangeConstant.DIRECT_KEY_INFO;
import static com.mwu.aitok.model.video.mq.VideoDirectExchangeConstant.EXCHANGE_VIDEO_DIRECT;
import static com.mwu.aitok.service.video.constants.HotVideoConstants.VIDEO_BEFORE_DAT30;
import static com.mwu.aitok.service.video.constants.InterestPushConstant.VIDEO_CATEGORY_VIDEOS_CACHE_KEY_PREFIX;
import static com.mwu.aitok.service.video.constants.InterestPushConstant.VIDEO_TAG_VIDEOS_CACHE_KEY_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class IVideoServiceImpl implements IVideoService {

    private final VideoRepository videoRepository;

    private InterestPushService interestPushService;


    private final MinioService minioService;
    private final VideoSensitiveRepository videoSensitiveRepository;

    private final VideoSensitiveService videoSensitiveService;
    private final IVideoCategoryRelationService videoCategoryRelationService;

    private final IVideoTagRelationService videoTagRelationService;

    private final IUserVideoCompilationRelationService userVideoCompilationRelationService;
    private final RedisService redisService;
    private final UserFollowVideoPushService userFollowVideoPushService;

    private final IVideoPositionService videoPositionService;
    private final IVideoImageService videoImageService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;

    private final RemoteBehaveService remoteBehaveService;




    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceGrpc;

    @GrpcClient("aitok-behave")
    private BehaveServiceGrpc.BehaveServiceBlockingStub behaveServiceGrpc;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);



    /**
     * 解决异步线程无法访问主线程的ThreadLocal
     *
     */
    /*
    InheritableThreadLocal 允许子线程在创建时继承父线程的值，所以用 new Thread(...) 启动的子线程可以直接读到父线程设置的用户 ID。
    但对线程池/复用线程（如 CompletableFuture.runAsync 默认使用的线程池）无效，因为线程是事先创建并复用的
    ，不会在任务提交时自动拷贝值。解决方案是显式捕获父线程的值并在任务执行前设置、执行后清理。下面示例演示三种情况：直接子线程继承、
    线程池任务无法继承、用包装器手动传递上下文
     */
    private static final ThreadLocal<Long> userIdThreadLocal = new InheritableThreadLocal<>();

    public static void setUserId(Long userId) {
        userIdThreadLocal.set(userId);
    }

    public static Long getUserId() {
        return userIdThreadLocal.get();
    }
    @Resource
    private FfmpegVideoService ffmpegVideoService;
    @Resource
    private VideoRedisBatchCache videoRedisBatchCache;
    @Resource
    private SnowFlake snowFlake;

    private final UserFollowRepository userFollowRepository;


    @Override
    public VideoUploadVO uploadVideo(MultipartFile file) throws Exception {

        String url = minioService.uploadFile(file);
        VideoUploadVO videoUploadVO = new VideoUploadVO();
        videoUploadVO.setVideoUrl(url);
        videoUploadVO.setOriginUrl(url);
        videoUploadVO.setVframe( url + QiniuVideoOssConstants.VIDEO_FRAME_1_END);
        return videoUploadVO;


    }

    /**
     * 发布视频
     * todo 分布式锁解决幂等性
     * todo 优化使用异步完成各个步骤
     *
     * @param videoPublishDto
     * @return
     */
    @RedissonLock(prefixKey = "redisson:video", key = "#videoPublishDto.videoUrl")
    @Transactional
    @Override
    public String videoPublish(VideoPublishDto videoPublishDto) {

        /*
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken) ) {
            System.out.println("No authentication found in SecurityContext");
            return "";
        }
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        String userId = jwt.getClaim("userid");
*/
        String userId = getUserId().toString();

        boolean b = sensitiveCheck(videoPublishDto.getVideoTitle() + videoPublishDto.getVideoDesc());
        if (b) {
            // 存在敏感词抛异常
            throw new CustomException(SENSITIVEWORD_ERROR);
        }

        // 判断发布类型 publicType 0视频1图文
        if (videoPublishDto.getPublishType().equals(PublishType.VIDEO.getCode())) {
            Video video = new Video();
            video.setUserId(Long.valueOf(userId));
            video.setCreateTime(LocalDateTime.now());
            video.setCreateBy(userId);
            video.setCoverImage(StringUtils.isNull(videoPublishDto.getCoverImage()) ? video.getVideoUrl() + VideoCacheConstants.VIDEO_VIEW_COVER_IMAGE_KEY : videoPublishDto.getCoverImage());;
            video = videoRepository.save(video);

            //前端不传不用处理 将前端传递的分类拷贝到关联表对象
            if (StringUtils.isNotNull(videoPublishDto.getCategoryId())) {
                VideoCategoryRelation videoCategoryRelation = BeanCopyUtils.copyBean(videoPublishDto, VideoCategoryRelation.class);
                // video_id存入VideoCategoryRelation（视频分类关联表）
                videoCategoryRelation.setVideoId(String.valueOf(video.getId()));
                // 再将videoCategoryRelation对象存入video_category_relation表中
                videoCategoryRelationService.saveVideoCategoryRelation(videoCategoryRelation);
                // 存入分类库
                interestPushService.cacheVideoToCategoryRedis(String.valueOf(video.getId()), Collections.singletonList(videoPublishDto.getCategoryId()));
            }

            // 视频标签处理
            // 视频标签限制个数，五个
            if (StringUtils.isNotNull(videoPublishDto.getVideoTags())) {
                if (videoPublishDto.getVideoTags().length > VideoConstants.VIDEO_TAG_LIMIT) {
                    log.error("视频标签大于5个，不做处理");
                } else {
                    videoTagRelationService.saveVideoTagRelationBatch(String.valueOf(video.getId()), videoPublishDto.getVideoTags());
                }
            }
            // 将video对象存入video表中


            // 关联视频合集
            if (StringUtils.isNotNull(videoPublishDto.getCompilationId())) {
                userVideoCompilationRelationService.videoRelateCompilation(String.valueOf(video.getId()), videoPublishDto.getCompilationId());
            }
            // 发布成功添加缓存
            redisService.setCacheObject(VIDEO_INFO_PREFIX + video.getId(), video);
            String videoId = String.valueOf(video.getId());
            // 发送rabbit消息
            rabbitTemplate.convertAndSend(ESSYNC_DELAYED_EXCHANGE, ESSYNC_ROUTING_KEY, videoId, message -> {
                // 添加延迟消息属性，设置1分钟
                message.getMessageProperties().setDelayLong(ESSYNC_DELAYED_TIME);
                return message;
            });
            log.debug(" ==> {} 发送了一条消息 ==> {}", ESSYNC_DELAYED_EXCHANGE, videoId);
            // 同步视频标签库
            interestPushService.cacheVideoToTagRedis(videoId, Arrays.asList(videoPublishDto.getVideoTags()));
            // 同步视频详情
            rabbitTemplate.convertAndSend(EXCHANGE_VIDEO_DIRECT, DIRECT_KEY_INFO, videoId);
            log.debug(" ==> {} send a message ==> {}", EXCHANGE_VIDEO_DIRECT, videoId);
            // 同步用户发件箱
            userFollowVideoPushService.pusOutBoxFeed(video.getUserId(), videoId, DateUtils.toDate(video.getCreateTime()).getTime());
            return videoId;

        } else if (videoPublishDto.getPublishType().equals(PublishType.IMAGE.getCode())) {
            // 发布为图文
            Video video = BeanCopyUtils.copyBean(videoPublishDto, Video.class);
            video.setUserId(Long.valueOf(userId));
            video.setCreateTime(LocalDateTime.now());
            video.setCreateBy(userId);

            // 设置图文封面，若为空则使用图片集合的第一条
            video.setCoverImage(StringUtils.isEmpty(videoPublishDto.getCoverImage()) ? videoPublishDto.getImageFileList()[0] : videoPublishDto.getCoverImage());
            video = videoRepository.save(video);
            String videoId = String.valueOf(video.getId());
            // 视频标签处理
            // 视频标签限制个数，五个
            if (StringUtils.isNotNull(videoPublishDto.getVideoTags())) {
                if (videoPublishDto.getVideoTags().length > VideoConstants.VIDEO_TAG_LIMIT) {
                    log.error("视频标签大于5个，不做处理");
                } else {
                    videoTagRelationService.saveVideoTagRelationBatch(videoId, videoPublishDto.getVideoTags());
                }
            }
            // 将video对象存入video表中


                // 关联视频合集
                if (StringUtils.isNotNull(videoPublishDto.getCompilationId())) {
                    userVideoCompilationRelationService.videoRelateCompilation(videoId, videoPublishDto.getCompilationId());
                }
                // 发布成功添加缓存
                redisService.setCacheObject(VIDEO_INFO_PREFIX + videoId, video);
                // 异步批量保存图片集合到mysql
            //Arrays.stream(videoPublishDto.getImageFileList()) 将图片文件列表转换为流，每个图片的 URL 都会被映射为一个异步任务。
            //接着，toArray(CompletableFuture[]::new) 将流中的所有 CompletableFuture 收集到一个数组中。这个数组被传递给 CompletableFuture
            // 最后，通过调用 allFutures.join()，主线程会阻塞，直到所有异步任务完成。这确保了在所有图片数据保存到数据库后，代码才会继续执行后续逻辑。
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(Arrays.stream(videoPublishDto.getImageFileList())
                        .map(url -> asyncSaveVideoImagesToDB(videoId, url)).toArray(CompletableFuture[]::new));
                allFutures.join();
                // 开始存储视频发布位置
                if (videoPublishDto.getPositionFlag().equals(PositionFlag.OPEN.getCode())) {
                    VideoPosition videoPosition = BeanCopyUtils.copyBean(videoPublishDto.getPosition(), VideoPosition.class);
                    videoPosition.setVideoId(videoId);
                  videoPositionService.save(videoPosition);
                }
                // 发送消息
                rabbitTemplate.convertAndSend(ESSYNC_DELAYED_EXCHANGE, ESSYNC_ROUTING_KEY, videoId, message -> {
                    // 添加延迟消息属性，设置1分钟
                    message.getMessageProperties().setDelayLong(ESSYNC_DELAYED_TIME);
                    return message;
                });
                log.debug(" ==> {} 发送了一条消息 ==> {}", ESSYNC_DELAYED_EXCHANGE, videoId);
                // 同步视频标签库
                interestPushService.cacheVideoToTagRedis(videoId, Arrays.asList(videoPublishDto.getVideoTags()));
                // 同步视频分类库

                // 同步视频观看量到redis

                // 同步用户发件箱
                userFollowVideoPushService.pusOutBoxFeed(video.getUserId(),videoId, DateUtils.toDate(video.getCreateTime()).getTime());
                return videoId;

        } else {
            return "";
        }




    }


    /**
     * 异步执行同步数据操作
     *CompletableFuture 是 Java 提供的用于表示异步计算结果的类，支持非阻塞地发起任务、链式处理、组合多个任务以及统一等待。常见用法包括：
     * 使用 CompletableFuture.runAsync 启动不返回结果的异步任务（返回 CompletableFuture<Void>）。
     * 使用 CompletableFuture.supplyAsync 启动有返回值的异步任务（返回 CompletableFuture<T>）。
     * 用 thenApply/thenCompose/thenCombine 对异步结果做链式转换或合并。
     * 用 CompletableFuture.allOf(...) 等待一组任务全部完成，再继续后续逻辑。
     * 用 exceptionally 或 handle 捕获并处理异步异常。
     * 用 join() 或 get() 在需要时阻塞等待结果（join() 会把受检异常封装为 CompletionException）
     * @param videoId
     * @return
     */
    public CompletableFuture<Void> asyncSaveVideoImagesToDB(String videoId, String imageUrl) {
        VideoImage videoImage = new VideoImage();
        videoImage.setVideoId(videoId);
        videoImage.setImageUrl(imageUrl);
        return CompletableFuture.runAsync(() -> videoImageService.save(videoImage));
    }
    private boolean sensitiveCheck(String str) {

        List<VideoSensitive> sensitiveList = videoSensitiveRepository.findAll();
        if (CollectionUtils.isEmpty(sensitiveList)) {
            return true;
        }
        List<String> sensitives = sensitiveList.stream().map(VideoSensitive::getSensitives).toList();

        SensitiveWordUtil.initMap(sensitives);
        //是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(str);
        // 存在敏感词
        return map.size() > 0;
    }
    @Override
    public Page<Video> queryMyVideoPage(VideoPageDto pageDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken) ) {
            System.out.println("No authentication found in SecurityContext");
            return Page.empty();
        }
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        String userId = jwt.getClaim("userid");
        pageDto.setUserId(Long.valueOf(userId));
        int pageNum = Math.max(1, pageDto.getPageNum());
        int pageSize = Math.max(1, pageDto.getPageSize());
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        return videoRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            predicates.add(cb.equal(root.get("delFlag"), 0)); // 未删除
            if (StringUtils.isNotEmpty(pageDto.getVideoTitle())) {
                predicates.add(cb.like(root.get("videoTitle"), "%" + pageDto.getVideoTitle().trim() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

    }

    @Override
    public PageData queryMyVideoPageForApp(VideoPageDto pageDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken) ) {
            System.out.println("No authentication found in SecurityContext");
            return PageData.emptyPage();
        }
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        String userId = jwt.getClaim("userid");
        pageDto.setUserId(Long.valueOf(userId));

        // 分页与排序（PageRequest 的页码从 0 开始）
        Pageable pageable = PageRequest.of(
                Math.max(0, pageDto.getPageNum() - 1),
                Math.max(1, pageDto.getPageSize()),
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        // 动态查询条件
        Specification<Video> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), pageDto.getUserId()));
            predicates.add(cb.equal(root.get("delFlag"), DelFlagEnum.EXIST.getCode()));
            if (StringUtils.isNotEmpty(pageDto.getVideoTitle())) {
                predicates.add(cb.like(root.get("videoTitle"), "%" + pageDto.getVideoTitle().trim() + "%"));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Page<Video> videoPage = videoRepository.findAll(spec, pageable);
        List<Video> records = videoPage.getContent();
        if (StringUtils.isNull(records) || records.isEmpty()) {
            return PageData.emptyPage();
        }

        List<MyVideoVO> myVideoVOList = BeanCopyUtils.copyBeanList(records, MyVideoVO.class);
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                myVideoVOList.stream().map(this::packageMyVideoVOAsync).toArray(CompletableFuture[]::new)
        );
        allFutures.join();

        return PageData.genPageData(myVideoVOList, videoPage.getTotalElements());


    }

    public CompletableFuture<Void> packageMyVideoVOAsync(MyVideoVO vo) {
        return CompletableFuture.runAsync(() -> packageMyUserVideoVO(vo));
    }


    // TODO here should have behave service to do grpc
    public void packageMyUserVideoVO(MyVideoVO vo) {
        NumRequest request = NumRequest.newBuilder()
                .setVideoId(Long.parseLong(vo.getVideoId()))
                .build();
        NumResponse response = behaveServiceGrpc.apiGetVideoLikeNum(request);
        Long likeNum = response.getNum();

        vo.setLikeNum(likeNum);
    }

    @Override
    public Page<Video> queryUserVideoPage(VideoPageDto pageDto) {
        if (StringUtils.isNull(pageDto.getUserId())) {
            return Page.empty();
        }

        return queryMyVideoPage(pageDto);
    }

    /**
     * 视频feed接口
     *
     * @param videoFeedDTO createTime
     * @return video
     */
    @Override
    public List<VideoVO> feedVideo(VideoFeedDTO videoFeedDTO) {
        LocalDateTime createTime = videoFeedDTO.getCreateTime();
        LocalDateTime time = StringUtils.isNull(createTime) ? LocalDateTime.now() : createTime;

        // 分页与排序：按 createTime 倒序，取 10 条
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createTime"));

        // Specification 替代 LambdaQueryWrapper
        Specification<Video> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("delFlag"), DelFlagEnum.EXIST.getCode()));
            predicates.add(cb.lessThan(root.get("createTime"), time));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Video> page = videoRepository.findAll(spec, pageable);
        List<Video> videoList = page.getContent();
        if (CollectionUtils.isEmpty(videoList)) {
            return Collections.emptyList();
        }

        List<VideoVO> videoVOList = BeanCopyUtils.copyBeanList(videoList, VideoVO.class);

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                videoVOList.stream()
                        .map(vo -> packageVideoVOAsync(vo, UserContext.getUserId()))
                        .toArray(CompletableFuture[]::new)
        );
        allFutures.join();

        return videoVOList;
    }

    /**
     * 根据ids查询视频
     *
     * @param videoIds
     * @return
     */
    @Override
    public List<Video> queryVideoByVideoIds(List<String> videoIds) {


        if (CollectionUtils.isEmpty(videoIds)) {
        return Collections.emptyList();
    }
    Specification<Video> spec = (root, query, cb) -> root.get("videoId").in(videoIds);
    return videoRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createTime"));

    }


    /**
     * 删除视频
     *
     * @param videoId
     */
    @Transactional
    @Override
    public boolean deleteVideoByVideoId(String videoId) {
        // 从视频表删除视频（单条） todo 还得验证当前登录用户
        boolean deleted = this.deleteVideoByUser(videoId);
        if (deleted) {
            // 删除相关redis缓存
            // 删除视频分类库记录
            List<Long> categoryIds = videoCategoryRelationService.queryVideoCategoryIdsByVideoId(videoId);
            if (!categoryIds.isEmpty()) {
                categoryIds.forEach(cid -> {
                    redisTemplate.opsForSet().remove(VIDEO_CATEGORY_VIDEOS_CACHE_KEY_PREFIX + cid, videoId);
                });
            }
            // 删除标签库该视频记录x
            List<Long> videoTagIds = videoTagRelationService.queryVideoTagIdsByVideoId(videoId);
            if (!videoTagIds.isEmpty()) {
                videoTagIds.forEach(tid -> {
                    redisTemplate.opsForSet().remove(VIDEO_TAG_VIDEOS_CACHE_KEY_PREFIX + tid, videoId);
                });
            }
            // 删除视频缓存
            redisService.deleteObject(VIDEO_INFO_PREFIX + videoId);
            // 删除热门视频记录缓存
            redisTemplate.opsForZSet().remove(VIDEO_HOT, videoId);
            // todo 删除视频观看量缓存

            // 删除图文视频图片数据
            videoImageService.deleteVideoImagesByVideoId(videoId);
            // 从视频分类表关联表删除信息
            videoCategoryRelationService.deleteRecordByVideoId(videoId);
            // 删除视频对应的es文档
//            remoteBehaveService.deleteVideoDocumentByVideoId(videoId);
            // 删除视频评论
            NumRequest request = NumRequest.newBuilder()
                    .setVideoId(Long.parseLong(videoId))
                    .build();

            behaveServiceGrpc.removeVideoCommentByVideoId(request);
            // 删除视频标签关联表
            videoTagRelationService.deleteRecordByVideoId(videoId);
            // 删除视频位置信息表
            videoPositionService.deleteRecordByVideoId(videoId);
            // 删除视频合集关联表
            userVideoCompilationRelationService.deleteRecordByVideoId(videoId);
            // 删除别的用户对此视频点赞、收藏记录

            behaveServiceGrpc.removeOtherLikeVideoBehaveRecord(request);
            behaveServiceGrpc.removeOtherFavoriteVideoBehaveRecord(request);
            // todo 删除 es 文档
        }
        return deleted;
    }


    /**
     * 隐式删除视频
     *
     * @param videoId
     * @return
     */
    @Override
    public boolean deleteVideoByUser(String videoId) {


       // Long userId = UserContext.getUserId();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken) ) {
            return false;
        }
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        String userId = jwt.getClaimAsString("userId");

        Video video = videoRepository.findByIdAndUserId(Long.valueOf(videoId), Long.valueOf(userId));
        if (video == null) {
            return false;
        }
        video.setDelFlag(DelFlagEnum.DELETED.getCode());
        videoRepository.save(video);
        return true;
    }

    @Override
    public List<Video> getVideoListLtCreateTime(LocalDateTime ctime) {

        if (ctime == null) {
            return Collections.emptyList();
        }
        return videoRepository.findByCreateTimeGreaterThanEqualAndDelFlag(
                ctime,
                DelFlagEnum.EXIST.getCode(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );



    }

    /**
     * 视频算分
     *
     * @param videoList
     * @return
     */
    @Override
    public List<HotVideoVO> computeHotVideoScore(List<Video> videoList) {
        List<HotVideoVO> hotVideoVOList = new ArrayList<>();
        if (!videoList.isEmpty()) {
            videoList.forEach(v -> {
                HotVideoVO hotVideoVO = BeanCopyUtils.copyBean(v, HotVideoVO.class);
                hotVideoVO.setScore(computeVideoScore(v));
                hotVideoVOList.add(hotVideoVO);
            });
        } else {
            return new ArrayList<>();
        }
        return hotVideoVOList;
    }

    // 视频算分
    private double computeVideoScore(Video video) {
        double score = 0;
        // 观看
        if (video.getViewNum() != null) {
            //获取redis中的浏览量
            score += video.getViewNum() * HotVideoConstants.WEIGHT_VIEW;
        }
        // 点赞
        Long likeCount = videoRepository.getLikeNumById(video.getId());
        score += likeCount * HotVideoConstants.WEIGHT_LIKE;
        // 收藏
        Long favoriteCount = videoRepository.getLikeNumById(video.getId());
        score += favoriteCount * HotVideoConstants.WEIGHT_FAVORITE;
        // 创建时间
        if (video.getCreateTime() != null) {
            LocalDateTime createTime = video.getCreateTime();
            Duration between = Duration.between(LocalDateTime.now(), createTime);
            long hours = between.toHours();
            // 计算的是7天的数据量，使用7天总的小时数减去这个差值
            long totalHour = VIDEO_BEFORE_DAT30 * 24;
            long realHour = totalHour - Math.abs(hours);
            score += Math.abs(realHour) * HotVideoConstants.WEIGHT_CREATE_TIME;
        }
        // 评论量
        Long commentCount = videoRepository.getCommentNumById(video.getId());
        score += commentCount * HotVideoConstants.WEIGHT_COMMENT;
        return score / 100;
    }

    /**
     * 视频总获赞量
     *
     * @param userId
     * @return
     */
    @Override
    public Long getVideoLikeAllNumByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        return videoRepository.sumLikeNumByUserIdAndDelFlag(userId, DelFlagEnum.EXIST.getCode());    }

    /**
     * 查询用户作品数量
     *
     * @return
     */
    @Override
    public Long queryUserVideoCount() {

        Long userId = UserContext.getUserId();
        if (userId == null) {
            return 0L;
        }
        return videoRepository.countByUserIdAndDelFlag(userId, DelFlagEnum.EXIST.getCode());
    }

    /**
     * 查询用户的作品
     *
     * @param pageDto
     * @return
     */
    @Override
    public Page<Video> queryMemberVideoPage(VideoPageDto pageDto) {
        int pageNum = Math.max(1, pageDto.getPageNum());
        int pageSize = Math.max(1, pageDto.getPageSize());
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Specification<Video> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), pageDto.getUserId()));
            predicates.add(cb.equal(root.get("delFlag"), 0));
            if (StringUtils.isNotEmpty(pageDto.getVideoTitle())) {
                predicates.add(cb.like(root.get("videoTitle"), "%" + pageDto.getVideoTitle().trim() + "%"));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return videoRepository.findAll(spec, pageable);
    }

    /**
     * @param pageDTO
     * @return
     */
    @Override
    public PageData getHotVideos(PageDTO pageDTO) {
        int startIndex = (pageDTO.getPageNum() - 1) * pageDTO.getPageSize();
        int endIndex = startIndex + pageDTO.getPageSize() - 1;
        Set<Object> videoIds = redisService.getCacheZSetRange(VideoCacheConstants.VIDEO_HOT, startIndex, endIndex);
        Long hotCount = redisService.getCacheZSetZCard(VideoCacheConstants.VIDEO_HOT);

        List<CompletableFuture<VideoVO>> futures = videoIds.parallelStream()
                .map(vid -> CompletableFuture.supplyAsync(() -> {

                    Video video = videoRepository.findById(Long.valueOf(vid.toString())).orElse(null);

                    assert video != null;
                    Integer unused = Math.toIntExact(video.getUserId());
                    MemberResponse author = memberServiceGrpc.getById(GetByIdRequest.newBuilder().setUserId(video.getUserId()).build());

                    Member user = BeanCopyUtils.copyBean(author, Member.class);
                    //List<Member> authors = videoRepository.batchSelectVideoAuthor(Collections.singletonList(video.getUserId()));
                    VideoVO videoVO = BeanCopyUtils.copyBean(video, VideoVO.class);
                    videoVO.setUserNickName(user.getNickName());
                    videoVO.setUserAvatar(user.getAvatar());
                    // todo 是否关注
                    videoVO.setHotScore(redisService.getZSetScore(VideoCacheConstants.VIDEO_HOT, (String) vid));
                    // 图文
                    if (videoVO.getPublishType().equals(PublishType.IMAGE.getCode())) {
                        Object imgsCacheObject = redisService.getCacheObject(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId());
                        if (StringUtils.isNotNull(imgsCacheObject)) {
                            if (imgsCacheObject instanceof JSONArray) {
                                JSONArray jsonArray = (JSONArray) imgsCacheObject;
                                videoVO.setImageList((String[]) jsonArray.toArray(new String[0]));
                            } else if (imgsCacheObject instanceof String) {
                                String jsonString = (String) imgsCacheObject;
                                videoVO.setImageList(JSON.parseObject(jsonString, String[].class));
                            }
                        } else {
                            List<VideoImage> videoImageList = videoImageService.queryImagesByVideoId(String.valueOf(videoVO.getId()));
                            String[] imgs = videoImageList.stream().map(VideoImage::getImageUrl).toArray(String[]::new);
                            videoVO.setImageList(imgs);
                            // 重建缓存
                            redisService.setCacheObject(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId(), imgs);
                            redisService.expire(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId(), 1, TimeUnit.DAYS);
                        }
                    }
                    return videoVO;
                })).collect(Collectors.toList());

        List<VideoVO> videoVOList = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        return PageData.genPageData(videoVOList, hotCount);

    }

    @Override
    public List<VideoVO> pushVideoList() {
        val user = UserContext.getUser();
        System.out.println("user = " + user);
        Member member;
        if (!UserContext.hasLogin()) {
            // 游客登陆
            member = new Member();
            member.setUserId(2L);
        } else {

            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Collections.emptyList();
            }
            MemberResponse memberResponse = memberServiceGrpc.getById(GetByIdRequest.newBuilder().setUserId(userId).build());
            member = BeanCopyUtils.copyBean(memberResponse, Member.class);
        }
        Collection<String> videoIdsByUserModel = interestPushService.getVideoIdsByUserModel(member);
        Collection<Long> videoIds = videoIdsByUserModel.stream().map(Long::valueOf).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(videoIdsByUserModel)) {
            return Collections.emptyList();
        }
        List<Video> videoList = videoRepository.findAllByIdIn(
                videoIds,
                Sort.by(Sort.Direction.DESC, "createTime")
        );
        List<VideoVO> videoVOList = BeanCopyUtils.copyBeanList(videoList, VideoVO.class);
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(videoVOList
                .stream()
                .map(this::packageUserVideoVOAsync)
                .toArray(CompletableFuture[]::new));
        allFutures.join();
        return videoVOList;
    }


    public void packageUserVideoVO(VideoVO videoVO) {
        CompletableFuture<Void> behaveDataFuture = packageVideoBehaveDataAsync(videoVO);
        CompletableFuture<Void> memberDataFuture = packageMemberDataAsync(videoVO);
        CompletableFuture<Void> imageDataFuture = packageVideoImageDataAsync(videoVO);
        CompletableFuture.allOf(
                behaveDataFuture,
                memberDataFuture,
                imageDataFuture
        ).join();
    }
    public CompletableFuture<Void> packageUserVideoVOAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageUserVideoVO(videoVO));
    }





    /**
     * 更新视频
     *
     * @param updateVideoDTO
     */
    @Override
    public boolean updateVideo(UpdateVideoDTO updateVideoDTO) {
        if (StringUtils.isEmpty(updateVideoDTO.getVideoId())) {
            return false;
        }
        Video dbVideo = videoRepository.findById(Long.valueOf(updateVideoDTO.getVideoId())).orElse(null);
        if (StringUtils.isNotNull(dbVideo) && UserContext.getUserId().equals(dbVideo.getUserId())) {

            Video video = videoRepository.findById(Long.valueOf(updateVideoDTO.getVideoId())).orElse(null);
            if (video == null) {
                return false;
            }
            // TODO check is null
            video.setVideoTitle(updateVideoDTO.getVideoTitle());
            video.setVideoDesc(updateVideoDTO.getVideoDesc());
            video.setCoverImage(updateVideoDTO.getCoverImage());
            video.setShowType(updateVideoDTO.getShowType());
            video.setPositionFlag(updateVideoDTO.getPositionFlag());
            videoRepository.save(video);


                // 修改成功更新缓存
                if (StringUtils.isNotEmpty(updateVideoDTO.getVideoTitle())) {
                    dbVideo.setVideoTitle(updateVideoDTO.getVideoTitle());
                }
                if (StringUtils.isNotEmpty(updateVideoDTO.getVideoDesc())) {
                    dbVideo.setVideoDesc(updateVideoDTO.getVideoDesc());
                }
                if (StringUtils.isNotEmpty(updateVideoDTO.getCoverImage())) {
                    dbVideo.setCoverImage(updateVideoDTO.getCoverImage());
                }
                if (StringUtils.isNotEmpty(updateVideoDTO.getShowType())) {
                    dbVideo.setShowType(updateVideoDTO.getShowType());
                }
                if (StringUtils.isNotEmpty(updateVideoDTO.getPositionFlag())) {
                    dbVideo.setPositionFlag(updateVideoDTO.getPositionFlag());
                }
                redisService.setCacheObject(VIDEO_INFO_PREFIX + updateVideoDTO.getVideoId(), dbVideo);
            return true;
        }
        return false;
    }

    /**
     * 根据视频ids查询
     *
     * @param videoIds
     * @return
     */
    @Override
    public List<Video> listByVideoIds(List<String> videoIds) {

        List<Video> videoList = new ArrayList<>();
        videoIds.forEach(videoId -> {
            Video video = videoRepository.findById(Long.valueOf(videoId)).orElse(null);
            if (video != null) {
                videoList.add(video);
            }
        });
        return videoList;

    }

    /**
     * 获取视频图文
     *
     * @param videoId
     * @return
     */
    @Override
    public String[] getVideoImages(String videoId) {
        List<VideoImage> videoImageList = videoImageService.queryImagesByVideoId(videoId);
        return videoImageList.stream().map(VideoImage::getImageUrl).toArray(String[]::new);
    }
    /**
     * 获取用户所有视频
     *
     * @param userId
     * @return
     */
    @Override
    public List<Video> getUserAllVideo(Long userId) {

        return videoRepository.findAllByUserId(userId);
    }

    /**
     * 获取所有未删除的视频
     *
     * @return
     */
    @Override
    public List<Video> getAllUnDeletedVideo() {
        return videoRepository.findAllByDelFlag(DelFlagEnum.EXIST.getCode());
    }



    /**
     * 相关视频推荐 todo
     *
     * @param videoId
     * @return
     */
    @Override
    public List<RelateVideoVO> getRelateVideoList(String videoId) {
        Set<String> resultVideoIds = new HashSet<>(); // 推荐结果
        int videoCount = 10; // 需要推荐的视频个数
        // 查询视频的标签
        List<Long> videoTagIds = videoTagRelationService.queryVideoTagIdsByVideoId(videoId);
        // 每个标签需要推荐的视频数
        int ceil = (int) Math.ceil((double) videoCount / videoTagIds.size());

        List<Callable<Void>> tasks = new ArrayList<>();
        for (Long tagId : videoTagIds) {
            tasks.add(() -> {
                String tagSetKey = VIDEO_TAG_VIDEOS_CACHE_KEY_PREFIX + tagId;
                List<Object> vIds = redisTemplate.opsForSet().randomMembers(tagSetKey, ceil);
                if (vIds != null && !vIds.isEmpty()) {
                    synchronized (resultVideoIds) {
                        resultVideoIds.addAll(vIds.stream().map(Object::toString).collect(Collectors.toList()));
                    }
                }
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        resultVideoIds.forEach(System.out::println);

        Map<String, Video> batch = videoRedisBatchCache.getBatch(new ArrayList<>(resultVideoIds));
        List<Video> videoList = new ArrayList<>(batch.values());
        List<RelateVideoVO> relateVideoVOS = BeanCopyUtils.copyBeanList(videoList, RelateVideoVO.class);
        relateVideoVOS.forEach(v -> {
            //    private VideoAuthor videoAuthor;
            //    private VideoBehave videoBehave;
            //    private VideoSocial videoSocial;

            MemberResponse memberResponse = memberServiceGrpc.getById(GetByIdRequest.newBuilder().setUserId(v.getUserId()).build());

            Member member = BeanCopyUtils.copyBean(memberResponse, Member.class);
            if (!Objects.isNull(member)) {
                v.setVideoAuthor(new RelateVideoVO.VideoAuthor(v.getUserId(), member.getNickName(), member.getAvatar()));
            }
            // 封装观看量、点赞数、收藏量
            Integer cacheViewNum = redisService.getCacheMapValue(VideoCacheConstants.VIDEO_VIEW_NUM_MAP_KEY, v.getVideoId());
            NumRequest request = NumRequest.newBuilder()
                    .setVideoId(Long.parseLong(v.getVideoId()))
                    .build();
            Long likeNum = behaveServiceGrpc.apiGetVideoLikeNum(request).getNum();
            Long favoriteNum = behaveServiceGrpc.apiGetVideoFavoriteNum(request).getNum();
            // 评论数
            Long commentNum = behaveServiceGrpc.apiGetVideoCommentNum(request).getNum();
            v.setVideoBehave(new RelateVideoVO.VideoBehave(StringUtils.isNull(cacheViewNum) ? 0L : cacheViewNum, StringUtils.isNull(likeNum) ? 0L : likeNum, StringUtils.isNull(favoriteNum) ? 0L : favoriteNum, StringUtils.isNull(commentNum) ? 0L : commentNum));
            // 社交数据
            v.setVideoSocial(new RelateVideoVO.VideoSocial(false));
        });
        return relateVideoVOS;
    }
    @Override
    public List<VideoRecommendVO> pushAppVideoList() {
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("Authentication Object: " + authentication.toString() + ", Type: " + authentication.getClass().getName());

        Member member = new Member();
       if (!(authentication instanceof JwtAuthenticationToken) ) {
           System.out.println("No authentication found in SecurityContext");
           member.setUserId(0L);
       } else {

           // Handle unauthenticated case if necessary

        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();



            String userId = jwt.getClaim("userid");
            GetByIdRequest request = GetByIdRequest.newBuilder().setUserId(Long.parseLong(userId)).build();
            // 执行 gRPC 请求
           System.out.println("Fetching user info for User ID: " + userId);
            MemberResponse response= memberServiceGrpc.getById(request);
           System.out.println("Received MemberResponse: " + response.toString());
            member = new Member();
            member.setUserId(response.getUserId());
            member.setUserName(response.getUserName());
            member.setNickName(response.getNickName());
            member.setAvatar(response.getAvatar());
            member.setEmail(response.getEmail());
            member.setTelephone(response.getTelephone());
            member.setSex(response.getSex());
            member.setStatus(response.getStatus());


        }
        Collection<String> videoIdsByUserModel = interestPushService.getVideoIdsByUserModel(member);

        System.out.println("Current User ID: " + member.getUserId());
        return List.of();
    }

    public CompletableFuture<Void> packageVideoRecommendVOAsync(VideoRecommendVO vo) {
        return CompletableFuture.runAsync(() -> packageVideoRecommendVO(vo));
    }

    public void packageVideoRecommendVO(VideoRecommendVO vo) {
        CompletableFuture<Void> videoDataFuture = packageVideoRecommendVOVideoDataAsync(vo);
        CompletableFuture<Void> behaveDataFuture = packageVideoRecommendVOBehaveDataAsync(vo);
        CompletableFuture<Void> memberDataFuture = packageVideoRecommendVODataAsync(vo);
        CompletableFuture.allOf(
                videoDataFuture,
                behaveDataFuture,
                memberDataFuture
        ).join();
    }

    public CompletableFuture<Void> packageVideoRecommendVOVideoDataAsync(VideoRecommendVO vo) {
        return CompletableFuture.runAsync(() -> packageVideoRecommendVOVideoData(vo));
    }

    public CompletableFuture<Void> packageVideoRecommendVOBehaveDataAsync(VideoRecommendVO vo) {
        return CompletableFuture.runAsync(() -> packageVideoRecommendVOBehaveData(vo));
    }

    public CompletableFuture<Void> packageVideoRecommendVODataAsync(VideoRecommendVO vo) {
        return CompletableFuture.runAsync(() -> packageVideoRecommendVOMemberData(vo));
    }

    /**
     * 封装视频数据
     */
    public void packageVideoRecommendVOVideoData(VideoRecommendVO vo) {
        // 图文视频
        if (vo.getPublishType().equals(PublishType.IMAGE.getCode())) {
            List<VideoImage> videoImageList = videoImageService.queryImagesByVideoId(vo.getVideoId());
            String[] imgs = videoImageList.stream().map(VideoImage::getImageUrl).toArray(String[]::new);
            vo.setImageList(imgs);
        }
    }
    /**
     * 封装视频行为数据
     */
    public void packageVideoRecommendVOBehaveData(VideoRecommendVO vo) {
        // 封装观看量、点赞数、收藏量
        Integer cacheViewNum = redisService.getCacheMapValue(VideoCacheConstants.VIDEO_VIEW_NUM_MAP_KEY, vo.getVideoId());
        vo.setViewNum(StringUtils.isNull(cacheViewNum) ? 0L : cacheViewNum);

        NumRequest request = NumRequest.newBuilder()
                .setVideoId(Long.parseLong(vo.getVideoId()))
                .build();
        Long likeNum = behaveServiceGrpc.apiGetVideoLikeNum(request).getNum();
        vo.setLikeNum( likeNum);
        Long favoriteNum = behaveServiceGrpc.apiGetVideoFavoriteNum(request).getNum();
        vo.setFavoriteNum(favoriteNum);
        // 评论数
        Long commentNum = behaveServiceGrpc.apiGetVideoCommentNum(request).getNum();
        vo.setCommentNum( commentNum);
    }

    /**
     * 封装作者数据
     */
    public void packageVideoRecommendVOMemberData(VideoRecommendVO vo) {
        // 封装用户信息
        Author author = new Author();
        Member userCache = redisService.getCacheObject("member:userinfo:" + vo.getUserId());
        if (StringUtils.isNotNull(userCache)) {
            author.setUserId(vo.getUserId());
            author.setUserName(userCache.getUserName());
            author.setNickName(userCache.getNickName());
            author.setAvatar(userCache.getAvatar());
        } else {
            MemberResponse memberResponse = memberServiceGrpc.getById(GetByIdRequest.newBuilder().setUserId(vo.getUserId()).build());
            Member publishUser = BeanCopyUtils.copyBean(memberResponse, Member.class);
            author.setUserId(vo.getUserId());
            author.setUserName(StringUtils.isNull(publishUser) ? "-" : publishUser.getUserName());
            author.setNickName(StringUtils.isNull(publishUser) ? "-" : publishUser.getNickName());
            author.setAvatar(StringUtils.isNull(publishUser) ? "" : publishUser.getAvatar());
        }
        vo.setAuthor(author);
    }


    @Override
    public VideoInfoVO getVideoInfoForApp(String videoId) {
        // 视频浏览量加一
        viewNumIncrement(videoId);
        Video video = videoRepository.findById(Long.valueOf(videoId)).orElse(null);
        if (Objects.isNull(video)) {
            return null;
        }
        VideoInfoVO videoInfoVO = BeanCopyUtils.copyBean(video, VideoInfoVO.class);
        // 视频作者
        MemberResponse memberResponse = memberServiceGrpc.getById(GetByIdRequest.newBuilder().setUserId(video.getUserId()).build());
        Member member = BeanCopyUtils.copyBean(memberResponse, Member.class);
        Author author = BeanCopyUtils.copyBean(Objects.isNull(member) ? new Member() : member, Author.class);
        videoInfoVO.setAuthor(author);
        // 社交-是否关注
        if (UserContext.hasLogin()) {
            Long loginUserId = UserContext.getUserId();
            videoInfoVO.setWeatherFollow(userFollowRepository.countByUserIdAndUserFollowId(loginUserId, videoInfoVO.getUserId()) > 0);
            videoInfoVO.setWeatherLike(remoteBehaveService.countByVideoIdAndUserId(Long.parseLong(videoInfoVO.getVideoId()), loginUserId) > 0);
            videoInfoVO.setWeatherFavorite(remoteBehaveService.countByVideoIdAndUserId(videoInfoVO.getVideoId(), loginUserId) > 0);
        }
        // 行为-观看、点赞、收藏、评论量；是否点赞、收藏
        Integer cacheViewNum = redisService.getCacheMapValue(VideoCacheConstants.VIDEO_VIEW_NUM_MAP_KEY, videoInfoVO.getVideoId());
        videoInfoVO.setViewNum(StringUtils.isNull(cacheViewNum) ? 0L : cacheViewNum);
        NumRequest request = NumRequest.newBuilder()
                .setVideoId(Long.parseLong(videoInfoVO.getVideoId()))
                .build();
        Long likeNum = behaveServiceGrpc.apiGetVideoLikeNum(request) .getNum();
        videoInfoVO.setLikeNum( likeNum);
        Long favoriteNum = behaveServiceGrpc.apiGetVideoFavoriteNum(request) .getNum();
        videoInfoVO.setFavoriteNum( favoriteNum);
        Long commentNum = behaveServiceGrpc.apiGetVideoCommentNum(request) .getNum();
        videoInfoVO.setCommentNum(commentNum);
        // 视频标签
        String[] tags = videoTagRelationService.queryVideoTags(videoInfoVO.getVideoId());
        videoInfoVO.setTags(tags);
        // 图文视频
        if (videoInfoVO.getPublishType().equals(PublishType.IMAGE.getCode())) {
            List<VideoImage> videoImageList = videoImageService.queryImagesByVideoId(videoInfoVO.getVideoId());
            String[] imgs = videoImageList.stream().map(VideoImage::getImageUrl).toArray(String[]::new);
            videoInfoVO.setImageList(imgs);
        }
        // 位置信息
        if (videoInfoVO.getPositionFlag().equals(PositionFlag.OPEN.getCode())) {
            VideoPosition videoPosition = videoPositionService.queryPositionByVideoId(videoInfoVO.getVideoId());
            videoInfoVO.setPosition(videoPosition);
        }
        return videoInfoVO;
    }

    @Override
    public void updateVideoInfo(String videoId) {

        Video video = videoRepository.findById(Long.valueOf(videoId)).orElse(null);
        if (Objects.isNull(video.getVideoUrl()) || video.getVideoUrl().isEmpty()) {
            log.debug("该视频url为空");
            return;
        }
        MultimediaInfo info = ffmpegVideoService.getVideoInfo(video.getVideoUrl());
        MediaVideoInfo mediaVideoInfo = new MediaVideoInfo(info);
        log.debug("视频详情：{}", mediaVideoInfo);
        Video videoUpdate = new Video();
        videoUpdate.setId(Long.valueOf(videoId));
        videoUpdate.setVideoInfo(new Gson().toJson(mediaVideoInfo));
        videoRepository.save(videoUpdate);
    }

    @Override
    public List<VideoVO> packageVideoVOByVideoIds(Long loginUserId, List<String> videoIds) {
        Map<String, Video> batch = videoRedisBatchCache.getBatch(new ArrayList<>(videoIds));
        List<Video> videoList = new ArrayList<>(batch.values());
        List<VideoVO> videoVOList = BeanCopyUtils.copyBeanList(videoList, VideoVO.class);
        // 封装VideoVO
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(videoVOList.stream()
                .map(vo -> packageVideoVOAsync(vo, loginUserId)).toArray(CompletableFuture[]::new));
        allFutures.join();
        return videoVOList;
    }

    @Override
    public List<Video> exitsByVideoIds(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Collections.emptyList();
        }
        // DelFlagEnum.EXIST.getCode() 返回 Integer 或相应类型

        List<Long> longVideoIds = videoIds.stream()
                .map(Long::valueOf)
                .toList();
        return videoRepository.findByIdInAndDelFlagOrderByCreateTimeDesc(longVideoIds, DelFlagEnum.EXIST.getCode());

    }

    @Override
    public VideoVO getVideoVOById(String videoId) {

        Video video = videoRedisBatchCache.get(videoId);
        VideoVO videoVO = new VideoVO();
        BeanUtils.copyProperties(video, videoVO);
        CompletableFuture<Void> future =  packageVideoVOAsync(videoVO, UserContext.getUserId());
        future.join();
        return videoVO;
    }

    /**
     * 浏览量自增1存入redis
     *
     * @param videoId
     */
    public void viewNumIncrement(String videoId) {
        log.debug("viewNumIncrement开始");
        if (StringUtils.isNotEmpty(videoId)) {
            redisService.incrementCacheMapValue(VideoCacheConstants.VIDEO_VIEW_NUM_MAP_KEY, videoId, 1);
        }
        log.debug("viewNumIncrement结束");
    }
    /**
     * 封装视频行为数据
     * TODO grpc replace dubbo
     * @param videoVO
     */
    public void packageVideoBehaveData(VideoVO videoVO) {
        log.debug("packageVideoBehaveData开始");
        // 封装观看量、点赞数、收藏量
        Integer cacheViewNum = redisService.getCacheMapValue(VideoCacheConstants.VIDEO_VIEW_NUM_MAP_KEY, videoVO.getVideoId());
        videoVO.setViewNum(StringUtils.isNull(cacheViewNum) ? 0L : cacheViewNum);
        NumRequest request = NumRequest.newBuilder()
                .setVideoId(Long.parseLong(videoVO.getVideoId()))
                .build();
        Long likeNum = behaveServiceGrpc.apiGetVideoLikeNum(request).getNum();
        videoVO.setLikeNum(likeNum);
        Long favoriteNum = behaveServiceGrpc.apiGetVideoFavoriteNum(request).getNum();
        videoVO.setFavoritesNum( favoriteNum);
        // 评论数
        Long commentNum = behaveServiceGrpc.apiGetVideoCommentNum(request).getNum();
        videoVO.setCommentNum( commentNum);
        log.debug("packageVideoBehaveData结束");
    }

    /**
     * 封装视频社交数据
     *
     * @param videoVO
     */

    public void packageVideoSocialData(VideoVO videoVO, Long loginUserId) {
        log.debug("packageVideoSocialData开始" + getUserId());
        if (StringUtils.isNotNull(loginUserId)) {
            // 是否关注、是否点赞、是否收藏
            videoVO.setWeatherLike(remoteBehaveService.countByVideoIdAndUserId(String.valueOf(videoVO.getId()), loginUserId) > 0);
            videoVO.setWeatherFavorite(remoteBehaveService.countByVideoIdAndUserId(videoVO.getId(), loginUserId) > 0);
            if (videoVO.getUserId().equals(loginUserId)) {
                videoVO.setWeatherFollow(true);
            } else {


                videoVO.setWeatherFollow(userFollowRepository.countByUserIdAndUserFollowId(loginUserId, videoVO.getUserId()) > 0);
            }
        }
        log.debug("packageVideoSocialData结束");
    }

    public void packagePushVideoVO(VideoVO videoVO) {
        log.debug("packagePushVideoVO");
        CompletableFuture<Void> memberDataFuture = packageMemberDataAsync(videoVO);
        CompletableFuture<Void> imageDataFuture = packageVideoImageDataAsync(videoVO);
        CompletableFuture.allOf(
                memberDataFuture,
                imageDataFuture
        ).join();
        log.debug("packagePushVideoVO");
    }
    /**
     * 封装用户数据
     *
     * @param videoVO
     */
    public void packageMemberData(VideoVO videoVO) {
        log.debug("packageMemberData开始");
        // 封装用户信息
        Member userCache = redisService.getCacheObject("member:userinfo:" + videoVO.getUserId());
        if (StringUtils.isNotNull(userCache)) {
            videoVO.setUserNickName(userCache.getNickName());
            videoVO.setUserAvatar(userCache.getAvatar());
        } else {

            MemberResponse memberResponse = memberServiceGrpc.getById(GetByIdRequest.newBuilder().setUserId(videoVO.getUserId()).build());
            Member publishUser = BeanCopyUtils.copyBean(memberResponse, Member.class);
            videoVO.setUserNickName(StringUtils.isNull(publishUser) ? "-" : publishUser.getNickName());
            videoVO.setUserAvatar(StringUtils.isNull(publishUser) ? null : publishUser.getAvatar());
        }
        log.debug("packageMemberData结束");
    }

    /**
     * 封装视频标签数据
     */
    public void packageVideoTagData(VideoVO videoVO) {
        log.debug("packageVideoTagData开始");
        // 封装标签返回
        String[] tags = videoTagRelationService.queryVideoTags(String.valueOf(videoVO.getId()));
        videoVO.setTags(tags);
        log.debug("packageVideoTagData结束");
    }

    /**
     * 封装图文数据
     *
     * @param videoVO
     */
    public void packageVideoImageData(VideoVO videoVO) {
        log.debug("packageVideoImageData开始");
        // 若是图文则封装图片集合
        if (videoVO.getPublishType().equals(PublishType.IMAGE.getCode())) {
            Object imgsCacheObject = redisService.getCacheObject(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId());
            if (StringUtils.isNotNull(imgsCacheObject)) {
                if (imgsCacheObject instanceof JSONArray jsonArray) {
                    videoVO.setImageList((String[]) jsonArray.toArray(new Object[0]));
                } else if (imgsCacheObject instanceof String) {
                    String jsonString = (String) imgsCacheObject;
                    videoVO.setImageList(JSON.parseObject(jsonString, String[].class));
                }
            } else {
                List<VideoImage> videoImageList = videoImageService.queryImagesByVideoId(String.valueOf(videoVO.getId()));
                String[] imgs = videoImageList.stream().map(VideoImage::getImageUrl).toArray(String[]::new);
                videoVO.setImageList(imgs);
                // 重建缓存
                redisService.setCacheObject(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId(), imgs);
                redisService.expire(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId(), 1, TimeUnit.DAYS);
            }
        }
        log.debug("packageVideoImageData结束");
    }

    /**
     * 封装视频定位数据
     *
     * @param videoVO
     */
    public void packageVideoPositionData(VideoVO videoVO) {
        log.debug("packageVideoPositionData开始");
        // 若是开启定位，封装定位
        if (videoVO.getPositionFlag().equals(PositionFlag.OPEN.getCode())) {
            // 查询redis缓存
            VideoPosition videoPositionCache = redisService.getCacheObject(VIDEO_POSITION_PREFIX_KEY + videoVO.getId());
            if (StringUtils.isNotNull(videoPositionCache)) {
               // TODO videoVO set position vo
                //  videoVO.setPosition(videoPositionCache);
            } else {
                VideoPosition videoPosition = videoPositionService.queryPositionByVideoId(String.valueOf(videoVO.getId()));
               // TODO videoVO set position vo
                // videoVO.setPosition(videoPosition);
                // 重建缓存
                redisService.setCacheObject(VIDEO_POSITION_PREFIX_KEY + videoVO.getId(), videoPosition);
                redisService.expire(VIDEO_POSITION_PREFIX_KEY + videoVO.getId(), 1, TimeUnit.DAYS);
            }
        }
        log.debug("packageVideoPositionData结束");
    }
    public CompletableFuture<Void> viewNumIncrementAsync(String videoId) {
        return CompletableFuture.runAsync(() -> viewNumIncrement(videoId));
    }

    public CompletableFuture<Void> packageVideoBehaveDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoBehaveData(videoVO));
    }

    public CompletableFuture<Void> packageVideoSocialDataAsync(VideoVO videoVO, Long loginUserId) {
        return CompletableFuture.runAsync(() -> packageVideoSocialData(videoVO, loginUserId));
    }

    public CompletableFuture<Void> packageMemberDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageMemberData(videoVO));
    }

    public CompletableFuture<Void> packageVideoTagDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoTagData(videoVO));
    }

    public CompletableFuture<Void> packageVideoImageDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoImageData(videoVO));
    }

    public CompletableFuture<Void> packageVideoPositionDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoPositionData(videoVO));
    }
    public CompletableFuture<Void> packageVideoVOAsync(VideoVO videoVO, Long loginUserId) {
        return CompletableFuture.runAsync(() -> packageVideoVO(videoVO, loginUserId));
    }
    public void packageVideoVO(VideoVO videoVO, Long loginUserId) {
        log.debug("packageVideoVO开始");
        CompletableFuture<Void> viewNumFuture = viewNumIncrementAsync(String.valueOf(videoVO.getId()));
        CompletableFuture<Void> behaveDataFuture = packageVideoBehaveDataAsync(videoVO);
        CompletableFuture<Void> socialDataFuture = packageVideoSocialDataAsync(videoVO, loginUserId);
        CompletableFuture<Void> memberDataFuture = packageMemberDataAsync(videoVO);
        CompletableFuture<Void> tagDataFuture = packageVideoTagDataAsync(videoVO);
        CompletableFuture<Void> imageDataFuture = packageVideoImageDataAsync(videoVO);
        CompletableFuture<Void> positionDataFuture = packageVideoPositionDataAsync(videoVO);
        CompletableFuture.allOf(
                viewNumFuture,
                behaveDataFuture,
                socialDataFuture,
                memberDataFuture,
                tagDataFuture,
                imageDataFuture,
                positionDataFuture
        ).join();
        log.debug("packageVideoVO结束");
    }


    @Override
    public List<Video> listByIds(List<String> videoIds) {
        List<Video> videoList = new ArrayList<>();

        videoIds.forEach(videoId -> {
            Optional<Video> videoget = videoRepository.findById(Long.valueOf(videoId));
            videoget.ifPresent(videoList::add);
            videoList.add(videoget.orElse(null));
        });
        return videoList;

    }
}
