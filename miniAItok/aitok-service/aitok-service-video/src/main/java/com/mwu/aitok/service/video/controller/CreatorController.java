package com.mwu.aitok.service.video.controller;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.creator.dto.VideoPageDTO;
import com.mwu.aitok.model.creator.dto.videoCompilationPageDTO;
import com.mwu.aitok.model.creator.vo.DashboardAmountVO;

import com.mwu.aitok.service.video.creator.service.CreatorService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * CreatorController
 *
 * @AUTHOR: mwu
 * @DATE: 2023/12/5
 **/
@RestController
@RequestMapping("/api/v1/creator")
public class CreatorController {

    @Resource
    private CreatorService creatorService;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;



    @GetMapping("/test")
    public String test() {
        return "test";
    }
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
    /*
    use this to test, postman not up to 5mb
    curl.exe -X POST "http://localhost:18008/api/v1/upload-video" `
         -H "Authorization: Bearer eyJraWQiOiI1ZTlkYTlkYi1lN2E1LTRjNWQtOTkzYy1hOTllNDM5MzgyMjEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ0ZXN0MzIiLCJhdWQiOiJhcHAiLCJpc3MiOiJhaXRvay1tZW1iZXIiLCJleHAiOjE3NjU4MDE1NjIsImlhdCI6MTc2NTc5Nzk2MiwidXNlcmlkIjoiMzAiLCJ1c2VybmFtZSI6InRlc3QzMiJ9.UXN4dRb7CQ4sdRrMFMMZMi1IqhB3a4WEGO6ERJHgJNs4Cd1cpe6nSJbrin5SScxKWT1CkHN0h-xTaZJQBu9O1jiehOm7TylZH2T1c7dqOwS22DHuPjMdjApB4jpAkxbaIdRoZ0hQ7e1kIo_Xtffy7cmx2qIMZaZwQoPb5jU1BNY6vVUT-kyaQoZWHSKc0g6EcoxCds7-oJ8eFW2LJAWshgNWqYEMOVuCwWZAwP44YOiP2FzWqGcHU_BprrcDRIi8XC6Lz3F2QtQ3fXZjJuiIN98rn51m4ykw-K-OTBweI1MPQUpVrO1zAjPFdQ2qMvUFmdXtZmC870OR4pXC30lp9A" `
         -F "file=@D:\testVide2.mp4;type=video/mp4"
     */
    @PostMapping("/upload-video")
    public R<String> uploadVideo(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.getSize() > Long.parseLong(maxFileSize.replace("MB", ""))*1024*1024) {
            return  R.fail( "文件大小超出限制");
        }
        return R.ok(creatorService.multipartUploadVideo(file));
    }

    /**
     * 视频播放量等流向数据
     */
    @GetMapping("/dashboard-amount")
    public R<DashboardAmountVO> dashboardAmount() {
        return R.ok(creatorService.dashboardAmount());
    }


}
