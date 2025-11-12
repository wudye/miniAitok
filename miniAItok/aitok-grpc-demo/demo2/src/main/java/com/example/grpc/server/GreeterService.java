package com.example.grpc.server;

import com.example.grpc.GreeterGrpc;
import com.example.grpc.HelloRequest;
import com.example.grpc.HelloResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;


@GrpcService
public class GreeterService extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        String name = request.getName();
        String message = "Hello, " + name + "!";
        HelloResponse response = HelloResponse.newBuilder().setMessage(message).build();

        // Send the response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}