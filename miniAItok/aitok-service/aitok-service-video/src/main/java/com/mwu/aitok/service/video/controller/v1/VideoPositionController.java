package com.mwu.aitok.service.video.controller.v1;

import com.mwu.aitok.service.video.service.IVideoPositionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 视频定位表(VideoPosition)表控制层
 *
 * @author mwu
 * @since 2023-11-21 15:44:14
 */
@RestController
@RequestMapping("/api/v1/videoPosition")
public class VideoPositionController {

    @Resource
    private IVideoPositionService videoPositionService;

}

