package com.mwu.atiokservice.behave.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.*;
import com.mwu.aitok.model.behave.domain.VideoUserLike;
import com.mwu.aitok.model.behave.enums.UserVideoBehaveEnum;
import com.mwu.aitok.model.behave.vo.app.MyLikeVideoVO;
import com.mwu.aitok.model.constants.VideoCacheConstants;
import com.mwu.aitok.model.member.domain.MemberInfo;
import com.mwu.aitok.model.member.enums.ShowStatusEnum;
import com.mwu.aitok.model.notice.domain.Notice;
import com.mwu.aitok.model.notice.enums.NoticeType;
import com.mwu.aitok.model.notice.enums.ReceiveFlag;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.domain.VideoImage;
import com.mwu.aitok.model.video.domain.VideoPosition;
import com.mwu.aitok.model.video.dto.VideoPageDto;
import com.mwu.aitok.model.video.enums.PositionFlag;
import com.mwu.aitok.model.video.enums.PublishType;
import com.mwu.aitok.model.video.vo.UserModel;
import com.mwu.aitok.model.video.vo.VideoVO;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.atiokservice.behave.repository.VideoUserLikeRepository;
import com.mwu.atiokservice.behave.service.IUserVideoBehaveService;
import com.mwu.atiokservice.behave.service.IVideoUserLikeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_CREATE_ROUTING_KEY;
import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_DIRECT_EXCHANGE;


/**
 * 点赞表(VideoUserLike)表服务实现类
 *
 * @author mwu
 * @since 2023-10-30 14:33:01
 */
@Slf4j
@Service("videoUserLikeService")
public class VideoUserLikeServiceImpl implements IVideoUserLikeService {

    @Resource
    private RedisService redisService;

    @Resource
    private VideoUserLikeRepository videoUserLikeMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;


    @GrpcClient("aitok-video")
    private VideoServiceGrpc.VideoServiceBlockingStub videoServiceBlockingStub;

    @Resource
    private IUserVideoBehaveService userVideoBehaveService;

    @Resource
    private ObjectMapper objectMapper;
    /**
     *
     * 向视频点赞表插入点赞信息
     *
     * @param videoId
     * @return
     */
    @Transactional
    @Override
    public boolean videoLike(String videoId) throws JsonProcessingException {
        Long userId = UserContext.getUserId();



        VideoUserLike one = videoUserLikeMapper.findByUserIdAndVideoId(userId, videoId);
        if (StringUtils.isNull(one)) {
            VideoUserLike videoUserLike = new VideoUserLike();
            videoUserLike.setVideoId(videoId);
            videoUserLike.setUserId(userId);
            videoUserLike.setCreateTime(LocalDateTime.now());
            // 将本条点赞信息存储到redis
            likeNumIncrement(videoId);
            // 发送消息，创建通知
            sendNoticeWithLikeVideo(videoId, userId);
            // 更新用户模型
            VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder().setVideoId(Long.parseLong(videoId)).build();
            TagsIdListResponse tagsIdListResponse = videoServiceBlockingStub.apiGetVideoTagIds(videoIdRequest);



            List<Long> tagIds = tagsIdListResponse.getTagIdsList();

            if (!tagIds.isEmpty()) {




                UserModelRequest userModelRequest = UserModelRequest.newBuilder()
                        .setUserId(userId)
                        .addAllTagIds(tagIds)
                        .setScore(1.0)
                        .build();

                videoServiceBlockingStub.apiUpdateUserModel(userModelRequest);

            }
            // 插入点赞行为数据
            userVideoBehaveService.syncUserVideoBehave(userId, videoId, UserVideoBehaveEnum.LIKE);
             videoUserLikeMapper.save(videoUserLike);
            return true;
        } else {
            // 取消点赞
            //将本条点赞信息从redis
            likeNumDecrement(videoId);
            videoUserLikeMapper.deleteByUserIdAndVideoId(userId, videoId);
            return true;
        }
    }

    /**
     * 发送用户点赞视频的消息
     *
     * @param videoId
     * @param operateUserId
     */
    @Async
    public void sendNoticeWithLikeVideo(String videoId, Long operateUserId) throws JsonProcessingException {
        // 根据视频获取发布者id
        VideoIdRequest request = VideoIdRequest.newBuilder().setVideoId(Long.parseLong(videoId)).build();
        VideoResponse videoResponse = videoServiceBlockingStub.apiGetVideoByVideoId(request);
        if (StringUtils.isNull(videoResponse)) {
            return;
        }
        Video video = BeanCopyUtils.copyBean(videoResponse, Video.class);

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
        notice.setContent("视频被人点赞了o.0");
        notice.setNoticeType(NoticeType.LIKE.getCode());
        notice.setReceiveFlag(ReceiveFlag.WAIT.getCode());
        notice.setCreateTime(LocalDateTime.now());
        // notice消息转换为json
        String msg = objectMapper.writeValueAsString(notice);
        rabbitTemplate.convertAndSend(NOTICE_DIRECT_EXCHANGE, NOTICE_CREATE_ROUTING_KEY, msg);
        log.debug(" ==> {} 发送了一条消息 ==> {}", NOTICE_DIRECT_EXCHANGE, msg);
    }

    /**
     * 缓存中点赞量自增一
     *
     * @param videoId
     */
    @Async
    public void likeNumIncrement(String videoId) {
        redisService.incrementCacheMapValue(VideoCacheConstants.VIDEO_LIKE_NUM_MAP_KEY, videoId, 1);
    }

    /**
     * 缓存中点赞量自减一
     *
     * @param videoId
     */
    @Async
    public void likeNumDecrement(String videoId) {
        redisService.incrementCacheMapValue(VideoCacheConstants.VIDEO_LIKE_NUM_MAP_KEY, videoId, -1);
    }

    /**
     * 分页查询我的点赞视频
     *
     * @param pageDto
     * @return
     */
    @Override
    public PageData queryMyLikeVideoPage(VideoPageDto pageDto) {
        pageDto.setPageNum((pageDto.getPageNum() - 1) * pageDto.getPageSize());
        pageDto.setUserId(UserContext.getUserId());
        Pageable pageable = PageRequest.of(pageDto.getPageNum() - 1, pageDto.getPageSize());
        List<Long> videoIds = videoUserLikeMapper.getVideoIdsByUserId(pageDto.getUserId());


        VideoIdListRequest videoIdListRequest = VideoIdListRequest.newBuilder().addAllVideoIds(videoIds).build();
        VideoListResponse videoListResponse = videoServiceBlockingStub.apiGetVideoListByVideoIds(videoIdListRequest);

        List<VideoResponse> recordlist = videoListResponse.getVideoListList();
        List<Video> records = BeanCopyUtils.copyBeanList(recordlist, Video.class);
        List<VideoVO> videoVOList = BeanCopyUtils.copyBeanList(records, VideoVO.class);
        Long userId = pageDto.getUserId();
        String videoTitle = pageDto.getVideoTitle();
        CompletableFuture.allOf(videoVOList.stream().map(this::packageVideoVOAsync).toArray(CompletableFuture[]::new)).join();
        return PageData.genPageData(videoVOList, videoUserLikeMapper.selectPersonLikeCount(userId, videoTitle));
    }

    @Async
    public CompletableFuture<Void> packageVideoVOAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoVO(videoVO));
    }

    @Async
    public void packageVideoVO(VideoVO videoVO) {
        CompletableFuture<Void> imageDataFuture = packageVideoImageDataAsync(videoVO);
        CompletableFuture.allOf(
                imageDataFuture
        ).join();
    }

    @Async
    public CompletableFuture<Void> packageVideoImageDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoImageData(videoVO));
    }

    @Override
    public PageData queryMyLikeVideoPageForApp(VideoPageDto pageDto) {
        pageDto.setPageNum((pageDto.getPageNum() - 1) * pageDto.getPageSize());
        pageDto.setUserId(UserContext.getUserId());
        List<Long> videoIds = videoUserLikeMapper.getVideoIdsByUserId(pageDto.getUserId());


        VideoIdListRequest videoIdListRequest = VideoIdListRequest.newBuilder().addAllVideoIds(videoIds).build();
        VideoListResponse videoListResponse = videoServiceBlockingStub.apiGetVideoListByVideoIds(videoIdListRequest);
        List<Video> records = List.of(); //videoUserLikeMapper.selectPersonLikePage(pageDto);
        List<MyLikeVideoVO> videoVOList = BeanCopyUtils.copyBeanList(records, MyLikeVideoVO.class);
        CompletableFuture.allOf(videoVOList.stream().map(this::packageMyLikeVideoPageAsync).toArray(CompletableFuture[]::new)).join();
        return PageData.genPageData(videoVOList, videoUserLikeMapper.selectPersonLikeCount(UserContext.getUserId(), pageDto.getVideoTitle()));
    }


    @Async
    public CompletableFuture<Void> packageMyLikeVideoPageAsync(MyLikeVideoVO vo) {
        return CompletableFuture.runAsync(() -> packageMyLikeVideoPage(vo));
    }

    @Async
    public void packageMyLikeVideoPage(MyLikeVideoVO vo) {
        vo.setLikeNum(getVideoLikeNum(vo.getVideoId()));
    }

    /**
     * 封装图文数据
     *
     * @param videoVO
     */
    @Async
    public void packageVideoImageData(VideoVO videoVO) {
        // 若是图文则封装图片集合
        if (videoVO.getPublishType().equals(PublishType.IMAGE.getCode())) {
            List<VideoImage> videoImageList = null;
            //videoUserLikeMapper.selectImagesByVideoId(videoVO.getVideoId());
            String[] imgs = videoImageList.stream().map(VideoImage::getImageUrl).toArray(String[]::new);
            videoVO.setImageList(imgs);
        }
    }

    /**
     * 查询用户的点赞列表
     *
     * @param pageDto
     * @return
     */
    @Override
    public PageData queryPersonLikePage(VideoPageDto pageDto) {
        //判断该用户的点赞列表是否对外展示
        MemberInfo memberInfo = null;
        // videoUserLikeMapper.selectPersonLikeShowStatus(pageDto.getUserId());
        if (memberInfo.getLikeShowStatus().equals(ShowStatusEnum.HIDE.getCode())) {
            return PageData.emptyPage();
        }
        pageDto.setPageNum((pageDto.getPageNum() - 1) * pageDto.getPageSize());
        List<Video> records = List.of();// videoUserLikeMapper.selectPersonLikePage(pageDto);
        ArrayList<VideoVO> videoVOList = new ArrayList<>();
        List<CompletableFuture<Void>> futures = records.stream()
                .map(r -> CompletableFuture.runAsync(() -> {
                    VideoVO videoVO = BeanCopyUtils.copyBean(r, VideoVO.class);
                    //若是图文，则封装图片集合
                    if (r.getPublishType().equals(PublishType.IMAGE.getCode())) {
                        List<VideoImage> videoImageList = null;// videoUserLikeMapper.selectImagesByVideoId(videoVO.getVideoId());
                        String[] imgs = videoImageList.stream().map(VideoImage::getImageUrl).toArray(String[]::new);
                        videoVO.setImageList(imgs);
                    }
                    //若是开启定位，则封装定位
                    if (r.getPositionFlag().equals(PositionFlag.OPEN.getCode())) {
                        VideoPosition videoPosition = null;
                              //  videoUserLikeMapper.selectPositionByVideoId(videoVO.getVideoId());
                        // TODO need set position VO add a class called videoPositionVO
                        // videoVO.setPosition(videoPosition);
                    }
                    videoVOList.add(videoVO);
                })).collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        //return PageData.genPageData(videoVOList, videoUserLikeMapper.selectPersonLikeCount(pageDto));
        return  null;
    }

    /**
     * 删除所有用户对此视频的点赞
     *
     * @param videoId
     * @return
     */
    @Override
    public boolean removeLikeRecordByVideoId(String videoId) {

        videoUserLikeMapper.deleteByVideoId(videoId);
        return true;
    }

    /**
     * 获取视频点赞数
     *
     * @param videoId
     * @return
     */
    @Override
    public Long getVideoLikeNum(String videoId) {
        return videoUserLikeMapper.getLikeNumByVideoId(videoId);
    }

    /**
     * 获取用户点赞视频id列表
     *
     * @param userId
     */
    @Override
    public List<String> getVideoIdsByUserId(Long userId) {

        List<VideoUserLike> videoUserLikes =videoUserLikeMapper.findByUserId(userId);
        return videoUserLikes.stream().map(VideoUserLike::getVideoId).collect(Collectors.toList());
    }

    /**
     * 视频点赞
     *
     * @param videoId
     */
    @Override
    public Boolean videoActionLike(String videoId) throws JsonProcessingException {
        /*
        Long userId = UserContext.getUserId();
        boolean likeVideo = videoUserLikeMapper.userLikeVideo(videoId, userId);
        if (likeVideo) {
            // 将本条点赞信息存储到redis
//            likeNumIncrement(videoId);
            // 发送消息，创建通知
            sendNoticeWithLikeVideo(videoId, userId);
            // 更新用户模型

            VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder().setVideoId(Long.parseLong(videoId)).build();
            TagsIdListResponse tagsIdListResponse = videoServiceBlockingStub.apiGetVideoTagIds(videoIdRequest);
            List<Long> tagIds = tagsIdListResponse.getTagIdsList();

            if (!tagIds.isEmpty()) {

                UserModelRequest userModelRequest = UserModelRequest.newBuilder()
                        .setUserId(userId)
                        .addAllTagIds(tagIds)
                        .setScore(1.0)
                        .build();

                videoServiceBlockingStub.apiUpdateUserModel(userModelRequest);
            }
            // 插入点赞行为数据
            userVideoBehaveService.syncUserVideoBehave(userId, videoId, UserVideoBehaveEnum.LIKE);
        }

        return likeVideo;
         */

        return true;
    }

    @Override
    public Boolean videoActionUnlike(String videoId) {

        videoUserLikeMapper.deleteByUserIdAndVideoId(UserContext.getUserId(), videoId);
        return true;
    }

    @Override
    public boolean weatherLikeVideo(String videoId, Long userId) {

        return videoUserLikeMapper.countByVideoIdAndUserId(videoId, userId)> 0;
    }
}
