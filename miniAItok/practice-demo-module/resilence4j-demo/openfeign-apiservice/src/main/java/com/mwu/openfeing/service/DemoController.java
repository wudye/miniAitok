package com.mwu.openfeing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @Autowired
    private ForFeign demoService;


    @GetMapping("/api/test")
    String getUser(@RequestParam(name="param") String param, @RequestParam(name="time") int time) {

        demoService.getUser(param, time);
        return "success";
    }

}
