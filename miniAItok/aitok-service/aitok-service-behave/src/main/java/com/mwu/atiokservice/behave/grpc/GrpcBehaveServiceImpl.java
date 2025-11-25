package com.mwu.atiokservice.behave.grpc;

import com.mwu.aitok.*;
import com.mwu.aitok.model.behave.domain.VideoUserLike;
import com.mwu.aitok.model.search.dubbo.VideoBehaveData;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.atiokservice.behave.repository.VideoUserLikeRepository;
import com.mwu.atiokservice.behave.service.IVideoUserCommentService;
import com.mwu.atiokservice.behave.service.IVideoUserFavoritesService;
import com.mwu.atiokservice.behave.service.IVideoUserLikeService;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.poi.ss.formula.functions.T;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GrpcService
public class GrpcBehaveServiceImpl extends BehaveServiceGrpc.BehaveServiceImplBase {


    @Resource
    private IVideoUserCommentService videoUserCommentService;

    @Resource
    private IVideoUserLikeService videoUserLikeService;

    @Autowired
    private VideoUserLikeRepository videoUserLikeRepository;

    @Resource
    private IVideoUserFavoritesService videoUserFavoritesService;



    public void apiGetVideoLikeNum(NumRequest request, StreamObserver<NumResponse> responseObserver) {


        Long videoId = request.getVideoId();

        Long res =  videoUserLikeService.getVideoLikeNum(String.valueOf(videoId));

        NumResponse response = NumResponse.newBuilder().setNum(res).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void apiGetVideoFavoriteNum(NumRequest request, StreamObserver<NumResponse> responseObserver) {

        Long videoId = request.getVideoId();

        Long res =  videoUserLikeRepository.getLikeNumByVideoId(String.valueOf(videoId));

        NumResponse response = NumResponse.newBuilder().setNum(res).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }


    public void apiGetVideoCommentNum(NumRequest request, StreamObserver<NumResponse> responseObserver) {
        Long videoId = request.getVideoId();
        Long res = videoUserCommentService.queryCommentCountByVideoId(String.valueOf(videoId));

        NumResponse response = NumResponse.newBuilder().setNum(res).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void apiDeleteVideoDocumentByVideoId(NumRequest request, StreamObserver<TrueResponse> responseObserver) {

        responseObserver.onNext(TrueResponse.newBuilder().setResult(true).build());
        responseObserver.onCompleted();
    }

    public void removeVideoCommentByVideoId(NumRequest request, StreamObserver<TrueResponse> responseObserver) {
        Long videoId = request.getVideoId();
        VideoUserLike videoUserLike =videoUserLikeRepository.findVideoUserLikeByVideoId(String.valueOf(videoId));
        if(videoUserLike!=null){
            videoUserLikeRepository.delete(videoUserLike);
        }
        responseObserver.onNext(TrueResponse.newBuilder().setResult(true).build());
        responseObserver.onCompleted();

    }

    public void removeOtherLikeVideoBehaveRecord(NumRequest request, StreamObserver<TrueResponse> responseObserver) {
        Long videoId = request.getVideoId();
        VideoUserLike videoUserLike =videoUserLikeRepository.findVideoUserLikeByVideoId(String.valueOf(videoId));
        if(videoUserLike!=null){
            videoUserLikeRepository.delete(videoUserLike);
        }
        responseObserver.onNext(TrueResponse.newBuilder().setResult(true).build());
        responseObserver.onCompleted();

    }

//    public void removeOtherLikeVideoBehaveRecord(NumRequest request, StreamObserver<NumResponse> responseObserver) {
//
//        Long videoId = request.getVideoId();
//        VideoUserLike videoUserLike =videoUserLikeRepository.findVideoUserLikeByVideoId(String.valueOf(videoId));
//        if(videoUserLike!=null){
//            videoUserLikeRepository.delete(videoUserLike);
//        }
//        responseObserver.onNext(TrueResponse.newBuilder().setResult(true).build());
//        responseObserver.onCompleted();
//    }


    public void removeOtherFavoriteVideoBehaveRecord(NumRequest request, StreamObserver<TrueResponse> responseObserver) {
        Long videoId = request.getVideoId();
        VideoUserLike videoUserLike =videoUserLikeRepository.findVideoUserLikeByVideoId(String.valueOf(videoId));
        if(videoUserLike!=null){
            videoUserLikeRepository.delete(videoUserLike);
        }
        responseObserver.onNext(TrueResponse.newBuilder().setResult(true).build());
        responseObserver.onCompleted();
    }

    public void apiWeatherLikeVideo(TwoNumRequest request, StreamObserver<TrueResponse> responseObserver) {
        String videoId = request.getVideoId();
        Long userId = request.getUserId();
         videoUserLikeService.weatherLikeVideo(videoId, userId);
        responseObserver.onNext(TrueResponse.newBuilder().setResult(true).build());
        responseObserver.onCompleted();
    }

    public void apiWeatherFavoriteVideo(TwoNumRequest request, StreamObserver<TrueResponse> responseObserver) {
        String videoId = request.getVideoId();
        Long userId = request.getUserId();
         videoUserFavoritesService.weatherFavoriteVideo(videoId, userId);
        responseObserver.onNext(TrueResponse.newBuilder().setResult(true).build());
        responseObserver.onCompleted();
    }

    public void apiGetVideoBehaveData(NumRequest request, StreamObserver<VideoBehaveDataResponse> responseObserver) {
        VideoBehaveData videoBehaveData = new VideoBehaveData();
        videoBehaveData.setViewCount(100L);
        String videoId = String.valueOf(request.getVideoId());
        videoBehaveData.setLikeCount(videoUserLikeService.getVideoLikeNum(videoId));
        videoBehaveData.setCommentCount(videoUserCommentService.queryCommentCountByVideoId(videoId));
        videoBehaveData.setFavoriteCount(videoUserFavoritesService.getFavoriteCountByVideoId(videoId));
        VideoBehaveDataResponse response = VideoBehaveDataResponse.newBuilder()
                .setViewNum(videoBehaveData.getViewCount())
                .setLikeNum(videoBehaveData.getLikeCount())
                .setCommentNum(videoBehaveData.getCommentCount())
                .setFavoriteNum(videoBehaveData.getFavoriteCount())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
