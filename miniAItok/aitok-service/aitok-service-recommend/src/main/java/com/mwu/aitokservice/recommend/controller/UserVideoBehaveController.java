package com.mwu.aitokservice.recommend.controller;

import com.mwu.aitokservice.recommend.service.IUserVideoBehaveService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户视频行为表(UserVideoBehave)表控制层
 *
 * @author roydon
 * @since 2024-04-27 18:56:46
 */
@RestController
@RequestMapping("/api/v1/userVideoBehave")
public class UserVideoBehaveController {

    @Resource
    private IUserVideoBehaveService userVideoBehaveService;

}

