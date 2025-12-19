package com.mwu.aitok.service.video.grpc;

import com.mwu.aitok.*;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.domain.VideoPosition;
import com.mwu.aitok.model.video.domain.VideoTag;
import com.mwu.aitok.model.video.vo.UserModel;
import com.mwu.aitok.model.video.vo.UserModelField;
import com.mwu.aitok.model.video.vo.UserVideoCompilationInfoVO;
import com.mwu.aitok.model.video.vo.VideoVO;
import com.mwu.aitok.service.video.repository.VideoRepository;

import com.mwu.aitok.service.video.service.*;
import com.mwu.aitok.service.video.service.cache.VideoRedisBatchCache;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
public class GrpcVideoServiceImpl extends VideoServiceGrpc.VideoServiceImplBase {


    @Resource
    private InterestPushService interestPushService;

    @Resource
    private IVideoService videoService;

    @Resource
    private VideoRepository videoMapper;

    @Resource
    private IVideoTagRelationService videoTagRelationService;

    @Resource
    private UserFollowVideoPushService userFollowVideoPushService;

    @Resource
    private IVideoPositionService videoPositionService;

    @Resource
    private VideoRedisBatchCache videoRedisBatchCache;

    @Resource
    private IUserVideoCompilationRelationService userVideoCompilationRelationService;

    @Resource
    private IUserVideoCompilationService userVideoCompilationService;




    @Override
    public void apiSyncVideoTagStack(ApiSyncVideoTagStackRequest request, StreamObserver<ApiSyncVideoTagStackResponse> responseObserver) {
        String videoId  = request.getVideoId();
        List<Long> tagsIds = request.getTagIdsList();

        interestPushService.cacheVideoToTagRedis(videoId, tagsIds);

        responseObserver.onNext(ApiSyncVideoTagStackResponse.newBuilder().setSuccess(true).setMessage("success").build());
        responseObserver.onCompleted();
    }

    /**
     * 通过id获取视频
     *
     * @param
     */
    @Override
    public void apiGetVideoByVideoId(VideoIdRequest request, StreamObserver<VideoResponse> responseObserver) {

        Long id = request.getVideoId();
        Optional<Video> video = videoMapper.findById(id);
        if (video.isPresent()) {
            VideoResponse videoResponse = VideoResponse.newBuilder()
                    .setVideoId(video.get().getId())
                    .setUserId(video.get().getUserId())
                    .setVideoTitle(video.get().getVideoTitle())
                    .setVideoDesc(video.get().getVideoDesc())
                    .setCoverImage(video.get().getCoverImage())
                    .setVideoUrl(video.get().getVideoUrl())
                    .setViewNum(video.get().getViewNum())
                    .setLikeNum(video.get().getLikeNum())
                    .setFavoritesNum(video.get().getFavoritesNum())

                    .build();
            responseObserver.onNext(videoResponse);

        } else {
            responseObserver.onNext(VideoResponse.newBuilder().build());
        }

        responseObserver.onCompleted();
    }

    /**
     * 通过ids获取视频
     *
     * @param
     */
    @Override
    public void apiGetVideoListByVideoIds(VideoIdListRequest request, StreamObserver<VideoListResponse> responseObserver) {
        List<Long> videoIds = request.getVideoIdsList();
        List<String> videoIdStrs = new ArrayList<>();
        for (Long vid : videoIds) {
            videoIdStrs.add(String.valueOf(vid));
        }
        Map<String, Video> batch = videoRedisBatchCache.getBatch(new ArrayList<>(videoIdStrs));

        List<Video> videoList = new ArrayList<>(batch.values());
        /*
        VideoListResponse videoListResponse = VideoListResponse.newBuilder()
                .addAllVideoList(BeanCopyUtils.copyBeanList(videoList, VideoResponse.class))
                .build();
        */


        responseObserver.onNext(protoConver(videoList));
        responseObserver.onCompleted();
    }

    private VideoListResponse protoConver(List<Video> videos) {

        VideoListResponse.Builder builder = VideoListResponse.newBuilder();
        if (videos == null) {
            return builder.build();
        }
        for (Video v : videos) {
            VideoResponse.Builder vr = VideoResponse.newBuilder()
                    .setVideoId(v.getId() == null ? 0L : v.getId())
                    .setUserId(v.getUserId() == null ? 0L : v.getUserId())
                    .setVideoTitle(v.getVideoTitle() == null ? "" : v.getVideoTitle())
                    .setVideoDesc(v.getVideoDesc() == null ? "" : v.getVideoDesc())
                    .setCoverImage(v.getCoverImage() == null ? "" : v.getCoverImage())
                    .setVideoUrl(v.getVideoUrl() == null ? "" : v.getVideoUrl())
                    .setViewNum(v.getViewNum() == null ? 0L : v.getViewNum())
                    .setLikeNum(v.getLikeNum() == null ? 0L : v.getLikeNum())
                    .setFavoritesNum(v.getFavoritesNum() == null ? 0L : v.getFavoritesNum());
            builder.addVideoList(vr.build());
        }
        return builder.build();

    }


    /**
     * 通过id获取视频图文
     *
     * @param
     *
     */


    public void apiGetVideoImagesByVideoId(VideoIdRequest request, StreamObserver<VideoImagesListResponse> responseObserver) {

        Long videoId = request.getVideoId();

        String[] videoImages =  videoService.getVideoImages(String.valueOf(videoId));

        VideoImagesListResponse.Builder videoImagesListResponse = VideoImagesListResponse.newBuilder();
        for (String img : videoImages) {
           VideoImagesResponse.Builder vir = VideoImagesResponse.newBuilder()
                   .setImageUrl(img);
           videoImagesListResponse.addVideoImagesList(vir.build());
        }
        responseObserver.onNext(videoImagesListResponse.build());
        responseObserver.onCompleted();

    }


    /**
     * 是否点赞某视频
     *
     * @param
     * @param
     * @return

    @Override
    public boolean apiWeatherLikeVideo(String videoId, Long userId) {
        return videoMapper.selectUserLikeVideo(videoId, userId) > 0;
    }
     */

    public void apiWeatherLikeVideo(ApiWeatherLikeVideoRequest request, StreamObserver<ApiWeatherLikeVideoResponse> responseObserver) {

        Long videoId = request.getVideoId();
        Long userId = request.getUserId();
        int like = videoMapper.getLikeNumByUserIdAndId(userId, videoId);
        if (like > 0) {
            responseObserver.onNext(ApiWeatherLikeVideoResponse.newBuilder().setLiked(true).setMessage("like").build());
        } else {
            responseObserver.onNext(ApiWeatherLikeVideoResponse.newBuilder().setLiked(false).setMessage("not like").build());
        }
        responseObserver.onCompleted();
    }


    /**
     * 是否收藏某视频
     *
     * @param
     * @param
     * @return

    @Override
    public boolean apiWeatherFavoriteVideo(String videoId, Long userId) {
        return videoMapper.userWeatherFavoriteVideo(videoId, userId) > 0;
    }

     */
    public void apiWeatherFavoriteVideo(ApiWeatherLikeVideoRequest request, StreamObserver<ApiWeatherLikeVideoResponse> responseObserver) {
        Long videoId = request.getVideoId();
        Long userId = request.getUserId();

        int favorite = videoMapper.getFavoritesByUserIdAndId(userId, videoId);
        if (favorite > 0) {
            responseObserver.onNext(ApiWeatherLikeVideoResponse.newBuilder().setLiked(true).setMessage("favorite").build());
        } else {
            responseObserver.onNext(ApiWeatherLikeVideoResponse.newBuilder().setLiked(false).setMessage("not favorite").build());
        }
        responseObserver.onCompleted();



    }







    /**
     * 获取视频标签ids
     *
     * @param
     * @return

    @Override
    public List<Long> apiGetVideoTagIds(String videoId) {
        return videoTagRelationService.queryVideoTagIdsByVideoId(videoId);
    }
     */

    public void apiGetVideoTagIds(VideoIdRequest request, StreamObserver<TagsIdListResponse> responseObserver) {
        Long videoId = request.getVideoId();


        List<Long> videoTagIds =  videoTagRelationService.queryVideoTagIdsByVideoId(String.valueOf(videoId));
       // TagsIdListResponse tagsIdListResponse = TagsIdListResponse.newBuilder().addAllTagIds(videoTagIds).build();
        TagsIdListResponse.Builder builder = TagsIdListResponse.newBuilder();
        for (Long tagId : videoTagIds) {
            builder.addTagIds(tagId);
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
    /**
     * 更新用户模型
     *
     * @param

    @Override
    public void apiUpdateUserModel(UserModel userModel) {

        interestPushService.updateUserModel(userModel);
    }
     */
    public void apiUpdateUserModel(UserModelRequest request, StreamObserver<ApiWeatherLikeVideoResponse> responseObserver) {

        UserModel userModel = new UserModel();
        userModel.setUserId(request.getUserId());
        userModel.setModels(request.getTagIdsList().stream().map(model -> {
            UserModelField userModelField = new UserModelField();
            userModelField.setTagId(model);

            return userModelField;
        }).collect(Collectors.toList()));
        interestPushService.updateUserModel(userModel);
        responseObserver.onNext(ApiWeatherLikeVideoResponse.newBuilder().setLiked(true).setMessage("update user model success").build());
        responseObserver.onCompleted();
    }

    /**
     * 初始化用户关注收件箱
     *
     * @param
     * @return

    @Override
    public void apiInitFollowVideoFeed(Long userId, List<Long> followIds) {
        userFollowVideoPushService.initFollowVideoFeed(userId, followIds);
    }

     */


    public void apiInitFollowVideoFeed(FollowVideoFeedRequest request, StreamObserver<ApiWeatherLikeVideoResponse> responseObserver) {
        Long userId = request.getUserId();
        List<Long> followIds = request.getFollowIdsList();

        userFollowVideoPushService.initFollowVideoFeed(userId, followIds);
        responseObserver.onNext(ApiWeatherLikeVideoResponse.newBuilder().setLiked(true).setMessage("init follow video feed success").build());
        responseObserver.onCompleted();
    }

    /**
     * 通过videoIds获取vo
     *
     * @param
     * @return

    @Override
    public List<VideoVO> apiGetVideoVOListByVideoIds(Long loginUserId, List<String> videoIds) {
        return videoService.packageVideoVOByVideoIds(loginUserId, videoIds);
    }
     */


    public void apiGetVideoVOListByVideoIds(VideoVORequest request, StreamObserver<VideoVOListResponse> responseObserver) {
        Long loginUserId = request.getLoginUserId();
        List<String> videoIds = request.getVideoIdsList();
        List<VideoVO> videoVOList = videoService.packageVideoVOByVideoIds(loginUserId, videoIds);
        VideoVOListResponse.Builder videoVOListResponse = VideoVOListResponse.newBuilder();
        for (VideoVO videoVO : videoVOList) {
            VideoVOResponse.Builder videoVOResponse = VideoVOResponse.newBuilder()
                    .setId(videoVO.getId())
                    .setUserId(videoVO.getUserId())
                    .setVideoTitle(videoVO.getVideoTitle())
                    .setVideoDesc(videoVO.getVideoDesc())
                    .setCoverImage(videoVO.getCoverImage())
                    .setVideoUrl(videoVO.getVideoUrl())
                    .setViewNum(videoVO.getViewNum())
                    .setLikeNum(videoVO.getLikeNum())
                    .setFavoritesNum(videoVO.getFavoritesNum());
            videoVOListResponse.addVideoVoList(videoVOResponse.build());

        }
        responseObserver.onNext(videoVOListResponse.build());
        responseObserver.onCompleted();
    }

    /**
     * 获取视频定位
     *
     * @param
     * @return

    @Override
    public VideoPosition apiGetVideoPositionByVideoId(String videoId) {
        return videoPositionService.queryPositionByVideoId(videoId);
    }

     */

    public void apiGetVideoPositionByVideoId(VideoIdRequest request, StreamObserver<VideoPositionResponse> responseObserver) {
        Long videoId = request.getVideoId();
        VideoPosition videoPosition = videoPositionService.queryPositionByVideoId(String.valueOf(videoId));
        VideoPositionResponse.Builder videoPositionResponse = VideoPositionResponse.newBuilder()
                .setVideoId(videoPosition.getVideoId())
                .setLongitude(videoPosition.getLongitude())
                .setLatitude(videoPosition.getLatitude())
                .setProvince(videoPosition.getProvince())
                .setCity(videoPosition.getCity())
                .setDistrict(videoPosition.getDistrict())
                .setAddress(videoPosition.getAddress());
        responseObserver.onNext(videoPositionResponse.build());
        responseObserver.onCompleted();
    }
    /**
     * 根据视频获取所在视频合集
     *
     * @param
     * @return

    @Override
    public UserVideoCompilationInfoVO apiGetUserVideoCompilationInfoVO(String videoId) {
        return userVideoCompilationService.getCompilationInfoVOByVideoId(videoId);
    }

     */


    public void apiGetUserVideoCompilationInfoVO(VideoIdRequest request, StreamObserver<UserVideoCompilationInfoVOResponse> responseObserver){

        Long videoId = request.getVideoId();
        UserVideoCompilationInfoVO videoCompilationInfoVO =  userVideoCompilationService.getCompilationInfoVOByVideoId(String.valueOf(videoId));
        UserVideoCompilationInfoVOResponse.Builder userVideoCompilationInfoVOResponse = UserVideoCompilationInfoVOResponse.newBuilder()
                .setCompilationId(videoCompilationInfoVO.getCompilationId())
                .setUserId(videoCompilationInfoVO.getUserId())
                .setPlayCount(videoCompilationInfoVO.getPlayCount())
                .setFavoriteCount(videoCompilationInfoVO.getFavoriteCount())
                .setWeatherFollow(videoCompilationInfoVO.getWeatherFollow())
                .setTitle(videoCompilationInfoVO.getTitle())
                .setDescription(videoCompilationInfoVO.getDescription())
                .setCoverImage(videoCompilationInfoVO.getCoverImage())
                .setVideoCount(videoCompilationInfoVO.getVideoCount());
        responseObserver.onNext( userVideoCompilationInfoVOResponse.build());
        responseObserver.onCompleted();
    }



    public void  apiGetVideoTagStack(VideoIdRequest request, StreamObserver<TagsIdListResponse> responseObserver) {
        Long videoId = request.getVideoId();

        List<VideoTag> videoTags =  videoTagRelationService.queryVideoTagsByVideoId(String.valueOf(videoId));

        TagsIdListResponse.Builder videoTagListResponse =
                TagsIdListResponse.newBuilder().addAllTagIds(
                        videoTags.stream().map(VideoTag::getTagId).collect(Collectors.toList())
                );



        responseObserver.onNext(videoTagListResponse.build());
        responseObserver.onCompleted();

    }

}
