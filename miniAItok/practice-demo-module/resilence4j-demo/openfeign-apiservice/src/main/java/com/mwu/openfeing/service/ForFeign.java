package com.mwu.openfeing.service;

import org.springframework.stereotype.Service;

@Service
public class ForFeign {
    public String getUser(String param, int time) {
        System.out.println("Feign Service called with param: " + param + " and time: " + time);
        return "Response from Feign Service with param: " + param + " after waiting " + time + "ms";
    }
}
