package com.mwu.demo1.userservice.service;
//
//import com.mwu.demo1.userservice.api.*;
//import io.grpc.stub.StreamObserver;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import net.devh.boot.grpc.server.service.GrpcService;
//
//@GrpcService
//public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {
//
//    private Logger logger = LoggerFactory.getLogger(getClass());
//
//    @Override
//    public void get(UserGetRequest request, StreamObserver<UserGetResponse> responseObserver) {
//        logger.info("[get]");
//        UserGetResponse.Builder builder = UserGetResponse.newBuilder();
//        builder.setId(request.getId())
//                .setName("no nickname set default as test：" + request.getId())
//                .setGender(request.getId() % 2 + 1);
//        responseObserver.onNext(builder.build());
//        responseObserver.onCompleted();
//
//
//    }
//
//    @Override
//    public void create(UserCreateRequest request, StreamObserver<UserCreateResponse> responseObserver) {
//        logger.info("[create]");
//        UserCreateResponse.Builder builder = UserCreateResponse.newBuilder();
//        builder.setId((int) (System.currentTimeMillis() / 1000));
//        System.out.println("create user id:" + builder.getId());
//        responseObserver.onNext(builder.build());
//        responseObserver.onCompleted();
//    }
//}


import com.mwu.demo1.userservice.api.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
public class UserServiceGrpcImpl extends UserServiceGrpc.UserServiceImplBase {



    private Logger logger = LoggerFactory.getLogger(getClass());

    // 内存存储：id -> UserGetResponse
    private final Map<Integer, UserGetResponse> store = new ConcurrentHashMap<>();


    @Override
    public void get(UserGetRequest request, StreamObserver<UserGetResponse> responseObserver) {
        logger.info("[get] id={}", request.getId());

        UserGetResponse user = store.get(request.getId());
        System.out.println("get user:" + user + " for id:" + request.getId());
        if (user == null) {
            // 回退到默认逻辑（兼容旧行为）
            UserGetResponse.Builder builder = UserGetResponse.newBuilder();
            builder.setId(request.getId())
                    .setName("no nickname set default as test：" + request.getId())
                    .setGender(request.getId() % 2 + 1);
            user = builder.build();

        }

        responseObserver.onNext(user);
        responseObserver.onCompleted();
    }

    @Override
    public void create(UserCreateRequest request, StreamObserver<UserCreateResponse> responseObserver) {
        logger.info("[create] name={}, gender={}", request.getName(), request.getGender());

        int id = (int) (System.currentTimeMillis() / 1000);
        UserCreateResponse.Builder createBuilder = UserCreateResponse.newBuilder();
        createBuilder.setId(id);

        // 构造并保存可被 later get() 查询的 UserGetResponse
        UserGetResponse user = UserGetResponse.newBuilder()
                .setId(id)
                .setName(request.getName() == null || request.getName().isEmpty()
                        ? ("no nickname set default as test：" + id)
                        : request.getName())
                .setGender(request.getGender() <= 0 ? (id % 2 + 1) : request.getGender())
                .build();

        store.put(id, user);

        System.out.println("create user id:" + createBuilder.getId());
        responseObserver.onNext(createBuilder.build());
        responseObserver.onCompleted();
    }
}
