package com.mwu.aitokservice.recommend.controller;


import com.mwu.aitiokcoomon.core.annotations.MappingCostTime;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.video.vo.VideoVO;
import com.mwu.aitokservice.recommend.service.VideoRecommendService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 视频推荐控制器
 *
 * @AUTHOR: mwu
 * @DATE: 2024/4/27
 **/
@RestController
@RequestMapping("/api/v1/video")
public class VideoRecommendController {

    @Resource
    private VideoRecommendService videoRecommendService;

    /**
     * 初始化推荐列表
     */
    @GetMapping("/init")
    public R<?> init() {
        return R.ok();
    }

    /**
     * 获取推荐视频流
     */
    @MappingCostTime
    @GetMapping("/feed")
    public R<List<VideoVO>> videoFeed() {
        return R.ok(videoRecommendService.pullVideoFeed());
    }

}
