package com.mwu.atiokservice.behave.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.*;
import com.mwu.aitok.model.behave.domain.UserFavoriteVideo;
import com.mwu.aitok.model.behave.domain.VideoUserFavorites;
import com.mwu.aitok.model.behave.enums.UserVideoBehaveEnum;
import com.mwu.aitok.model.behave.vo.UserFavoriteVideoVO;
import com.mwu.aitok.model.behave.vo.app.MyFavoriteVideoVO;
import com.mwu.aitok.model.constants.VideoCacheConstants;
import com.mwu.aitok.model.notice.domain.Notice;
import com.mwu.aitok.model.notice.enums.NoticeType;
import com.mwu.aitok.model.notice.enums.ReceiveFlag;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.domain.VideoImage;
import com.mwu.aitok.model.video.dto.VideoPageDto;
import com.mwu.aitok.model.video.enums.PublishType;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.atiokservice.behave.repository.UserFavoriteVideoRepository;
import com.mwu.atiokservice.behave.repository.VideoUserFavoritesRepository;
import com.mwu.atiokservice.behave.repository.VideoUserLikeRepository;
import com.mwu.atiokservice.behave.service.IUserFavoriteVideoService;
import com.mwu.atiokservice.behave.service.IUserVideoBehaveService;
import com.mwu.atiokservice.behave.service.IVideoUserFavoritesService;
import com.mwu.atiokservice.behave.service.IVideoUserLikeService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_CREATE_ROUTING_KEY;
import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_DIRECT_EXCHANGE;


/**
 * 视频收藏表(VideoUserFavorites)表服务实现类
 *
 * @author lzq
 * @since 2023-10-31 15:57:38
 */
@Slf4j
@Service("videoUserFavoritesService")
public class VideoUserFavoritesServiceImpl implements IVideoUserFavoritesService {
    @Resource
    private VideoUserFavoritesRepository videoUserFavoritesMapper;

    @Resource
    private UserFavoriteVideoRepository userFavoriteVideoMapper;

    @Resource
    private RedisService redisService;

    @Resource
    private VideoUserLikeRepository videoUserLikeMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Lazy // 解决循环依赖问题
    private IUserFavoriteVideoService userFavoriteVideoService;

    @GrpcClient("aitok-video")
    private VideoServiceGrpc.VideoServiceBlockingStub videoServiceBlockingStub;


    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private IVideoUserLikeService videoUserLikeService;

    @Resource
    private IUserVideoBehaveService userVideoBehaveService;

    /**
     * 用户收藏
     */
    @Transactional
    @Override
    public boolean userOnlyFavoriteVideo(String videoId) {
        //从token获取用户id
        Long userId = UserContext.getUserId();
        //构建查询条件
        ;
        //判断当前表中有没有记录
        List<VideoUserFavorites> list = videoUserFavoritesMapper.findByVideoIdAndUserId(videoId, userId);
        if (StringUtils.isNull(list) || list.isEmpty()) {
            //没有记录，则新建对象存入数据库
            VideoUserFavorites videoUserFavorites = new VideoUserFavorites();
            videoUserFavorites.setVideoId(videoId);
            videoUserFavorites.setUserId(userId);
            videoUserFavorites.setCreateTime(LocalDateTime.now());
            //将本条点赞信息存储到redis（key为videoId,value为videoUrl）
            favoriteNumIncrease(videoId);
            // 发送消息到通知
            sendNotice2MQ(videoId, userId);
            // 更新用户模型

            VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder().setVideoId(Long.parseLong(videoId)).build();
            TagsIdListResponse tagsIdListResponse = videoServiceBlockingStub.apiGetVideoTagIds(videoIdRequest);
            List<Long> tagIds = tagsIdListResponse.getTagIdsList();

            UserModelRequest userModelRequest = UserModelRequest.newBuilder()
                    .setUserId(userId)
                    .addAllTagIds(tagIds)
                    .setScore(2.0)
                    .build();

            videoServiceBlockingStub.apiUpdateUserModel(userModelRequest);


            // 插入收藏行为数据
            userVideoBehaveService.syncUserVideoBehave(userId, videoId, UserVideoBehaveEnum.FAVORITE);
             videoUserFavoritesMapper.save(videoUserFavorites);
            return true;
        }
        return false;
    }

    /**
     * 取消收藏
     */
    @Override
    public boolean userUnFavoriteVideo(String videoId) {
        //将本条点赞信息从redis移除
        favoriteNumDecrease(videoId);
        //如果收藏夹中有此视频，同时移除

        userFavoriteVideoMapper.deleteByUserIdAndVideoId(UserContext.getUserId(), videoId);

        return true;
    }



    /**
     * 用户收藏视频，通知mq
     *
     * @param videoId
     * @param operateUserId
     */
    private void sendNotice2MQ(String videoId, Long operateUserId) throws JsonProcessingException {
        // 根据视频获取发布者id
        Video video = videoUserLikeMapper.selectVideoByVideoId(videoId);
        if (StringUtils.isNull(video)) {
            return;
        }
        if (operateUserId.equals(video.getUserId())) {
            return;
        }
        // 封装notice实体
        Notice notice = new Notice();
        notice.setOperateUserId(operateUserId);
        notice.setNoticeUserId(video.getUserId());
        notice.setVideoId(videoId);
        notice.setContent("视频被人收藏了0.o");
        notice.setNoticeType(NoticeType.FAVORITE.getCode());
        notice.setReceiveFlag(ReceiveFlag.WAIT.getCode());
        notice.setCreateTime(LocalDateTime.now());
        // notice消息转换为json
        String msg = objectMapper.writeValueAsString(notice);
        rabbitTemplate.convertAndSend(NOTICE_DIRECT_EXCHANGE, NOTICE_CREATE_ROUTING_KEY, msg);
        log.debug(" ==> {} 发送了一条消息 ==> {}", NOTICE_DIRECT_EXCHANGE, msg);
    }

    /**
     * @param pageDto
     * @return
     */
    @Override
    public Page<VideoUserFavorites> queryFavoritePage(VideoPageDto pageDto) {

        Pageable pageable = PageRequest.of(pageDto.getPageNum() - 1, pageDto.getPageSize(),  Sort.by(Sort.Direction.DESC, "createTime"));
        Page<VideoUserFavorites> page = videoUserFavoritesMapper.findAll(pageable);
        return page;
    }

    /**
     * 分页查询用户收藏的视频
     *
     * @param pageDto
     * @return
     */
    @Override
    public PageData queryUserFavoriteVideoPage(VideoPageDto pageDto) {
        pageDto.setPageNum((pageDto.getPageNum() - 1) * pageDto.getPageSize());
        pageDto.setUserId(UserContext.getUserId());
        // 查询用户收藏的视频列表（包含分页）
        Pageable pageable = PageRequest.of(pageDto.getPageNum(), pageDto.getPageSize(),  Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UserFavoriteVideo> page = userFavoriteVideoMapper.findAllByUserId(pageDto.getUserId(), pageable);
        List<UserFavoriteVideo> userFavoriteVideos = page.getContent();
        if (userFavoriteVideos.isEmpty()) {
            return PageData.emptyPage();
        }

        List<UserFavoriteVideoVO> videos = userFavoriteVideos.stream().map(r -> BeanCopyUtils.copyBean(r, UserFavoriteVideoVO.class)).collect(Collectors.toList());
        // 收集图文视频的视频ID
        List<String> imageVideoIds = videos.stream()
                .filter(r -> r.getPublishType().equals(PublishType.IMAGE.getCode()))
                .map(UserFavoriteVideoVO::getVideoId)
                .collect(Collectors.toList());
        // 批量查询图文视频对应的图片集合（异步）
        CompletableFuture<List<VideoImage>> videoImagesFuture = CompletableFuture.supplyAsync(() ->
                videoUserLikeMapper.selectImagesByVideoIds(imageVideoIds));
        // 更新视频对象的图片集合
        CompletableFuture<Void> updateVideosFuture = videoImagesFuture.thenAcceptAsync(videoImages -> {
            if (videoImages != null) {
                Map<String, List<VideoImage>> videoImageMap = videoImages.stream()
                        .collect(Collectors.groupingBy(VideoImage::getVideoId));
                videos.forEach(r -> {
                    if (r.getPublishType().equals(PublishType.IMAGE.getCode())) {
                        List<VideoImage> videoImageList = videoImageMap.getOrDefault(r.getVideoId(), Collections.emptyList());
                        String[] imgs = videoImageList.stream()
                                .map(VideoImage::getImageUrl)
                                .toArray(String[]::new);
                        r.setImageList(imgs);
                    }
                });
            }
        });
        // 等待异步操作完成
        CompletableFuture.allOf(videoImagesFuture, updateVideosFuture).join();
        // 查询用户收藏的视频总数
        Long count = videoUserFavoritesMapper.selectUserFavoriteVideosCount(pageDto);
        return PageData.genPageData(videos, count);
    }

    @Override
    public PageData queryMyFavoriteVideoPageForApp(VideoPageDto pageDto) {
        pageDto.setPageNum((pageDto.getPageNum() - 1) * pageDto.getPageSize());
        pageDto.setUserId(UserContext.getUserId());
        // 查询用户收藏的视频列表（包含分页）
        List<String> videoIds = videoUserFavoritesMapper.selectUserFavoriteVideoIds(pageDto);
        if (videoIds.isEmpty()) {
            return PageData.emptyPage();
        }

        VideoIdListRequest videoIdListRequest = VideoIdListRequest.newBuilder().addAllVideoIds(
                videoIds.stream().map(Long::parseLong).collect(Collectors.toList())
        ).build();
        VideoListResponse videoListResponse = videoServiceBlockingStub.apiGetVideoListByVideoIds(videoIdListRequest);

        List<Video> videos = videoListResponse.getVideoListList().stream().map(videoProto -> {
            Video video = new Video();
           video =  BeanCopyUtils.copyBean(videoProto, Video.class);
            return video;
        }).toList();

        // 查询视频
        List<MyFavoriteVideoVO> myFavoriteVideoVOList = BeanCopyUtils.copyBeanList(videos, MyFavoriteVideoVO.class);
        // 设置点赞量
        CompletableFuture.allOf(myFavoriteVideoVOList.stream().map(this::packageMyLikeVideoPageAsync).toArray(CompletableFuture[]::new)).join();
        // 查询用户收藏的视频总数
        Long count = videoUserFavoritesMapper.selectUserFavoriteVideosCount(pageDto);
        return PageData.genPageData(myFavoriteVideoVOList, count);
    }

    @Override
    public PageData queryUserFavoriteVideoPageForApp(VideoPageDto pageDto) {
        pageDto.setPageNum((pageDto.getPageNum() - 1) * pageDto.getPageSize());
        // 查询用户收藏的视频列表（包含分页）
        List<String> videoIds = videoUserFavoritesMapper.selectUserFavoriteVideoIds(pageDto);
        if (videoIds.isEmpty()) {
            return PageData.emptyPage();
        }
        // 查询视频
        VideoIdListRequest videoIdListRequest = VideoIdListRequest.newBuilder().addAllVideoIds(
                videoIds.stream().map(Long::parseLong).collect(Collectors.toList())
        ).build();
        VideoListResponse videoListResponse = videoServiceBlockingStub.apiGetVideoListByVideoIds(videoIdListRequest);

        List<Video> videos = videoListResponse.getVideoListList().stream().map(videoProto -> {
            Video video = new Video();
            video =  BeanCopyUtils.copyBean(videoProto, Video.class);
            return video;
        }).toList();
        List<MyFavoriteVideoVO> myFavoriteVideoVOList = BeanCopyUtils.copyBeanList(videos, MyFavoriteVideoVO.class);
        // 设置点赞量
        CompletableFuture.allOf(myFavoriteVideoVOList.stream().map(this::packageMyLikeVideoPageAsync).toArray(CompletableFuture[]::new)).join();
        // 查询用户收藏的视频总数
        Long count = videoUserFavoritesMapper.selectUserFavoriteVideosCount(pageDto);
        return PageData.genPageData(myFavoriteVideoVOList, count);
    }

    @Async
    public CompletableFuture<Void> packageMyLikeVideoPageAsync(MyFavoriteVideoVO vo) {
        return CompletableFuture.runAsync(() -> packageMyLikeVideoPage(vo));
    }

    @Async
    public void packageMyLikeVideoPage(MyFavoriteVideoVO vo) {
        vo.setLikeNum(videoUserLikeService.getVideoLikeNum(vo.getVideoId()));
    }


    @Async
    public void favoriteNumIncrease(String videoId) {
        redisService.incrementCacheMapValue(VideoCacheConstants.VIDEO_FAVORITE_NUM_MAP_KEY, videoId, 1);
    }

    @Async
    public void favoriteNumDecrease(String videoId) {
        redisService.incrementCacheMapValue(VideoCacheConstants.VIDEO_FAVORITE_NUM_MAP_KEY, videoId, -1);
    }

    /**
     * 删除说有用户收藏此视频记录 ！！！
     *
     * @param videoId
     * @return
     */
    @Transactional
    @Override
    public boolean removeFavoriteRecordByVideoId(String videoId) {
        // 删除仅收藏
        videoUserFavoritesMapper.deleteByVideoId(videoId);
        // 从收藏夹删除
        userFavoriteVideoMapper.deleteByVideoId(videoId);
        return true;
    }

    @Override
    public Long getFavoriteCountByVideoId(String videoId) {
        return videoUserFavoritesMapper.countByVideoId(videoId);
    }

    /**
     * 获取用户收藏视频列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<String> getFavoriteVideoIdListByUserId(Long userId) {

        List<VideoUserFavorites> videoUserFavorites = videoUserFavoritesMapper.findByUserId(userId);
        if (videoUserFavorites.isEmpty()) {
            return Collections.emptyList();
        }

        return videoUserFavorites.stream().map(VideoUserFavorites::getVideoId).collect(Collectors.toList());
    }

    /**
     * 是否收藏视频
     *
     * @param videoId
     * @param userId
     * @return
     */
    @Override
    public boolean weatherFavoriteVideo(String videoId, Long userId) {
        long count1 = (long) videoUserFavoritesMapper.countByVideoIdAndUserId(videoId, userId);
        long count2 = (long) videoUserFavoritesMapper.countByVideoIdAndUserId(videoId, userId);
        return count1 > 0 || count2 > 0;
    }
}
