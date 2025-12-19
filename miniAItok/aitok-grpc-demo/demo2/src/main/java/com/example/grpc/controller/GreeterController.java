package com.example.grpc.controller;

import com.example.grpc.client.GreeterClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreeterController {

    @Autowired
    GreeterClient greeterClient;

    @GetMapping("/greet")
    public String greet(@RequestParam String name) {
        return greeterClient.sayHello(name);
    }
}