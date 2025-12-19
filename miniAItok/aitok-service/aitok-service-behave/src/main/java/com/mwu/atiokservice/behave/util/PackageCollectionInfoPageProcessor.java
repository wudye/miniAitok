package com.mwu.atiokservice.behave.util;

import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitok.VideoIdRequest;
import com.mwu.aitok.VideoResponse;
import com.mwu.aitok.VideoServiceGrpc;
import com.mwu.aitok.model.behave.vo.UserFavoriteInfoVO;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.atiokservice.behave.repository.UserFavoriteRepository;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class PackageCollectionInfoPageProcessor {

    @Resource
    private UserFavoriteRepository userFavoriteMapper;



    @GrpcClient("aitok-video")
    private VideoServiceGrpc.VideoServiceBlockingStub videoServiceBlockingStub;
    private static final int collectionCoverLimit = 6; // 收藏夹详情默认展示六张视频封面

    public void processUserFavoriteInfoList(List<UserFavoriteInfoVO> collectionInfoList) {
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(collectionInfoList.stream()
                .map(this::packageUserFavoriteInfoVOAsync).toArray(CompletableFuture[]::new));
        allFutures.join();
    }

    public CompletableFuture<Void> packageUserFavoriteInfoVOAsync(UserFavoriteInfoVO userFavoriteInfoVO) {
        return CompletableFuture.runAsync(() -> packageUserFavoriteInfoVO(userFavoriteInfoVO));
    }

    public void packageUserFavoriteInfoVO(UserFavoriteInfoVO userFavoriteInfoVO) {
        CompletableFuture<Void> packageCollectionVideoCountFuture = packageCollectionVideoCountAsync(userFavoriteInfoVO);
        CompletableFuture<Void> packageCollectionCoverListFuture = packageCollectionCoverListAsync(userFavoriteInfoVO);
        CompletableFuture.allOf(
                packageCollectionVideoCountFuture,
                packageCollectionCoverListFuture
        ).join();
    }

    public CompletableFuture<Void> packageCollectionVideoCountAsync(UserFavoriteInfoVO userFavoriteInfoVO) {
        return CompletableFuture.runAsync(() -> collectionVideoCount(userFavoriteInfoVO));
    }

    public CompletableFuture<Void> packageCollectionCoverListAsync(UserFavoriteInfoVO userFavoriteInfoVO) {
        return CompletableFuture.runAsync(() -> collectionCoverList(userFavoriteInfoVO));
    }

    /**
     * 收藏夹视频总数
     */
    public void collectionVideoCount(UserFavoriteInfoVO userFavoriteInfoVO) {
        userFavoriteInfoVO.setVideoCount(userFavoriteMapper.countById(userFavoriteInfoVO.getFavoriteId()));
    }

    /**
     * 获取前六张封面
     */
    public void collectionCoverList(UserFavoriteInfoVO userFavoriteInfoVO) {



        Long userIdi = userFavoriteInfoVO.getUserId();
        List<Long> favoriteVideos = userFavoriteMapper.findByIdOrderByCreateTimeDesc(userIdi);
        List<Long> favoriteVideoIds = favoriteVideos.size() <= 6
                ? new ArrayList<>(favoriteVideos)
                : new ArrayList<>(favoriteVideos.subList(0, 6));

        List<String> videoCovers = new ArrayList<>();


        for (Long videoId : favoriteVideoIds) {
            VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder().setVideoId(videoId).build();

            VideoResponse videoResponse = videoServiceBlockingStub.apiGetVideoByVideoId(videoIdRequest);
            Video video = BeanCopyUtils.copyBean(videoResponse, Video.class);
            videoCovers.add(video.getCoverImage());
        }

        String[] videoCoverArray = new String[videoCovers.size()];
        videoCovers.toArray(videoCoverArray);
        userFavoriteInfoVO.setVideoCoverList(videoCoverArray);
    }
}
