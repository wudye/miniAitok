package com.mwu.aitokservice.creator.service;


import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.creator.dto.VideoPageDTO;
import com.mwu.aitok.model.creator.dto.videoCompilationPageDTO;
import com.mwu.aitok.model.creator.vo.DashboardAmountVO;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * CreatorService
 *
 * @AUTHOR: mwu
 * @DATE: 2023/12/5
 **/
public interface CreatorService {
    /**
     * 视频分页
     *
     * @param videoPageDTO
     * @return
     */
    PageData queryVideoPage(VideoPageDTO videoPageDTO);

    /**
     * 视频合集分页
     *
     * @param videoCompilationPageDTO
     * @return
     */
    PageData queryVideoCompilationPage(videoCompilationPageDTO videoCompilationPageDTO);

    /**
     * 上传图文视频图片
     *
     * @param file
     * @return
     */
    String uploadVideoImage(MultipartFile file) throws Exception;

    /**
     * 上传视频
     *
     * @param file
     * @return
     */
    String uploadVideo(MultipartFile file) throws Exception;

    /**
     * 分片上传视频
     *
     * @param file
     * @return
     */
    String multipartUploadVideo(MultipartFile file) throws Exception;

    /**
     * 视频播放量
     */
    DashboardAmountVO dashboardAmount();

}
