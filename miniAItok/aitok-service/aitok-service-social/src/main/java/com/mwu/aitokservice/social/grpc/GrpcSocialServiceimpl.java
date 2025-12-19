package com.mwu.aitokservice.social.grpc;

import com.mwu.aitok.*;
import com.mwu.aitokservice.social.service.IUserFollowService;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;

public class GrpcSocialServiceimpl extends SocialServiceGrpc.SocialServiceImplBase {


    @Resource
    private IUserFollowService userFollowService;


    public void apiWeatherFollow(FellowRequest request, StreamObserver<FellowResponse> responseObserver) {
        Long userId = request.getUserId();
        Long followUserId = request.getFollowUserId();
        boolean followCount = userFollowService.weatherFollow(userId, followUserId);

        FellowResponse response = FellowResponse.newBuilder()
                .setSuccess(followCount).setMessage("follow status retrieved successfully")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    public void apiUserFollowCount(CountRequest request, StreamObserver<CountResponse> responseObserver) {
        Long userId = request.getUserId();
        Long followCount = userFollowService.getUserFollowCount(userId);
        CountResponse response = CountResponse.newBuilder()
                .setCount(followCount)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    public void apiUserFansCount(CountRequest request, StreamObserver<CountResponse> responseObserver) {
        Long userId = request.getUserId();
        Long fansCount = userFollowService.getUserFansCount(userId);
        CountResponse response = CountResponse.newBuilder()
                .setCount(fansCount)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

