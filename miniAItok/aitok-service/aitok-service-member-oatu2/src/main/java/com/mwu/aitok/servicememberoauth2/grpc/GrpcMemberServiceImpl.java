package com.mwu.aitok.servicememberoauth2.grpc;

import com.mwu.aitok.*;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.servicememberoauth2.constants.UserCacheConstants;
import com.mwu.aitok.servicememberoauth2.service.MemberService;
import com.mwu.aitokcommon.cache.service.RedisService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class GrpcMemberServiceImpl extends MemberServiceGrpc.MemberServiceImplBase {

    private final MemberService memberService;
    private final RedisService redisService;


    @Override
    public void getInIds(GetInIdsRequest request, StreamObserver<MemberListResponse> responseObserver) {
        List<Long> userIds = request.getUserIdsList();
        MemberListResponse memberListResponse = MemberListResponse.newBuilder()
                .addAllMembers(userIds.stream().map(id -> {
                    Member member = redisService.getCacheObject(UserCacheConstants.USER_INFO_PREFIX + id);
                    if (member == null) {
                        member = memberService.getByMemberId(id);
                    }
                    return toProto(member);
                }).toList())
                .build();
        responseObserver.onNext(memberListResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getByUserName(GetByUserNameRequest request, StreamObserver<MemberResponse> responseObserver) {
        String userName = request.getUserName();

        Member memeber = memberService.getByUserName(userName);

        responseObserver.onNext(toProto(memeber));
        responseObserver.onCompleted();
    }

    @Override
    public void getById(GetByIdRequest request, StreamObserver<MemberResponse> responseObserver) {
        Long memberId = request.getUserId();

        Member memeber = redisService.getCacheObject(UserCacheConstants.USER_INFO_PREFIX + memberId);
        if (memeber == null) {
            memeber = memberService.getByMemberId(memberId);
        }

        responseObserver.onNext(toProto(memeber));
        responseObserver.onCompleted();

    }

    @Override
    public void getAvatar(GetAvatarRequest request, StreamObserver<AvatarResponse> responseObserver) {
        Long memberId = request.getUserId();

        Member memeber = redisService.getCacheObject(UserCacheConstants.USER_INFO_PREFIX + memberId);
        if (memeber == null) {
            memeber = memberService.getByMemberId(memberId);
        }
        if(memeber == null) {
            responseObserver.onNext(AvatarResponse.newBuilder().build());
        }
        else{
            responseObserver.onNext(AvatarResponse.newBuilder().setAvatar(memeber.getAvatar()).build()  ) ;
        }

        responseObserver.onCompleted();
    }

    private MemberResponse toProto(Member memeber) {
        if (memeber == null) {
            return MemberResponse.newBuilder().build();
        }
        return MemberResponse.newBuilder()
                .setUserId(memeber.getUserId())
                .setUserName(Optional.ofNullable(memeber.getUserName()).orElse(""))
                .setNickName(Optional.ofNullable(memeber.getNickName()).orElse(""))
                .setEmail(Optional.ofNullable(memeber.getEmail()).orElse(""))
                .setTelephone(Optional.ofNullable(memeber.getTelephone()).orElse(""))
                .setSex(Optional.ofNullable(memeber.getSex()).orElse(""))
                .setAvatar(Optional.ofNullable(memeber.getAvatar()).orElse(""))
                .setStatus(Optional.ofNullable(memeber.getStatus()).orElse("0"))
                .build();

    }
}
