package com.mwu.aitokservice.search.controller.app;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitokservice.search.domain.VideoSearchHistory;
import com.mwu.aitokservice.search.service.VideoSearchHistoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AppVideoSearchHistoryController
 *
 * @AUTHOR: roydon
 * @DATE: 2024/2/3
 * 视频搜索历史
 **/
@RestController
@RequestMapping("/api/v1/app/history")
public class AppVideoSearchHistoryController {

    @Resource
    private VideoSearchHistoryService videoSearchHistoryService;

    /**
     * app端搜索历史
     */
    @GetMapping("/load")
    public R<List<VideoSearchHistory>> findUserSearchHistoryForApp() {
        return R.ok(videoSearchHistoryService.findAppSearchHistory());
    }

}
