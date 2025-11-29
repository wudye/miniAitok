package com.mwu.aitok.service.video.controller.v1;

import com.mwu.aitok.service.video.service.IVideoImageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 视频图片关联表(VideoImage)表控制层
 *
 * @author mwu
 * @since 2023-11-20 21:18:59
 */
@RestController
@RequestMapping("/api/v1/videoImage")
public class VideoImageController {

    @Resource
    private IVideoImageService videoImageService;


}

