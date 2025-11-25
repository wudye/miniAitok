package com.mwu.aitokservice.creator.controller.v1;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.creator.dto.VideoPageDTO;
import com.mwu.aitok.model.creator.dto.videoCompilationPageDTO;
import com.mwu.aitok.model.creator.vo.DashboardAmountVO;
import com.mwu.aitokservice.creator.service.CreatorService;
import io.minio.errors.*;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * CreatorController
 *
 * @AUTHOR: roydon
 * @DATE: 2023/12/5
 **/
@RestController
@RequestMapping("/api/v1")
public class CreatorController {

    @Resource
    private CreatorService creatorService;

    /**
     * 视频分页
     */
    @PostMapping("/videoPage")
    public PageData videoPage(@RequestBody VideoPageDTO videoPageDTO) {
        return creatorService.queryVideoPage(videoPageDTO);
    }

    /**
     * 视频合集分页
     */
    @PostMapping("/videoCompilationPage")
    public PageData videoCompilationPage(@RequestBody videoCompilationPageDTO videoCompilationPageDTO) {
        return creatorService.queryVideoCompilationPage(videoCompilationPageDTO);
    }

    /**
     * 上传视频图文图片
     */
    @PostMapping("/upload-video-image")
    public R<String> uploadVideoImage(@RequestParam("file") MultipartFile file) throws Exception {
        return R.ok(creatorService.uploadVideoImage(file));
    }

    /**
     * 上传视频
     */
    @PostMapping("/upload-video")
    public R<String> uploadVideo(@RequestParam("file") MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return R.ok(creatorService.multipartUploadVideo(file));
    }

    /**
     * 视频播放量等流向数据
     */
    @GetMapping("/dashboard-amount")
    public R<DashboardAmountVO> dashboardAmount() {
        return R.ok(creatorService.dashboardAmount());
    }

//    /**
//     * 测试分片上传视频
//     * success
//     */
//    @PostMapping("/testMultipartUploadVideo")
//    public R<String> multipartUploadVideoFile(@RequestParam("file") MultipartFile file) {
//        return R.ok(creatorService.multipartUploadVideo(file));
//    }
}
