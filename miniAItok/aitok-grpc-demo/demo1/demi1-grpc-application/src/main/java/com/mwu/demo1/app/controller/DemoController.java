package com.mwu.demo1.app.controller;

import com.mwu.demo1.userservice.api.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceGrpc;

    @GetMapping
    public String demo()
    {
        return "demo1 demo";
    }
    @GetMapping("/get")
    public String get(@RequestParam("id") Integer id) {
        // 创建请求
        UserGetRequest request = UserGetRequest.newBuilder().setId(id).build();
        // 执行 gRPC 请求
        UserGetResponse response = userServiceGrpc.get(request);
        // 响应
        return response.getName();
    }

//    message UserCreateRequest {
//        string name = 1;
//        int32 gender = 2;
//    }
//
//    message UserCreateResponse {
//        int32 id = 1;
//    }
//rpc create(UserCreateRequest) returns (UserCreateResponse);

    @GetMapping("/create") // 为了方便测试，实际使用 @PostMapping
    public Integer create(@RequestParam("name") String name,
                          @RequestParam("gender") Integer gender) {
        // 创建请求
        UserCreateRequest request = UserCreateRequest.newBuilder()
                .setName(name).setGender(gender).build();
        // 执行 gRPC 请求
        UserCreateResponse response = userServiceGrpc.create(request);
        // 响应
        return response.getId();
    }

}