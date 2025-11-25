package com.mwu.aitolk.feign.video;


import com.mwu.aitiokcoomon.core.constant.ServiceNameConstants;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitolk.feign.video.fallback.RemoteVideoServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * RemoteVideoService
 *
 * @AUTHOR: roydon
 * @DATE: 2023/10/28
 **/
@FeignClient(contextId = "remoteVideoService", value = ServiceNameConstants.VIDEO_SERVICE, fallbackFactory = RemoteVideoServiceFallback.class)
public interface RemoteVideoService {

    @GetMapping("/api/v1/{videoIds}")
    R<List<Video>> queryVideoByVideoIds(@PathVariable("videoIds") List<String> videoIds);



}
