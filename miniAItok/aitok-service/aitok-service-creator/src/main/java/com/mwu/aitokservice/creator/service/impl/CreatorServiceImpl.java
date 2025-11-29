package com.mwu.aitokservice.creator.service.impl;

import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import com.mwu.aitok.model.creator.dto.VideoPageDTO;
import com.mwu.aitok.model.creator.dto.videoCompilationPageDTO;
import com.mwu.aitok.model.creator.vo.DashboardAmountItem;
import com.mwu.aitok.model.creator.vo.DashboardAmountVO;
import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitokservice.creator.repository.UserVideoCompilationRepository;
import com.mwu.aitokservice.creator.repository.VideoRepository;

import com.mwu.aitokservice.creator.service.CreatorService;
import com.mwu.aitokstarter.file.service.MinioService;
import io.minio.errors.*;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * CreatorServiceImpl
 *
 * @AUTHOR: mwu
 * @DATE: 2023/12/5
 **/
@Slf4j
@Service
public class CreatorServiceImpl implements CreatorService {

    @Resource
    private VideoRepository videoMapper;
    @Resource
    private UserVideoCompilationRepository userVideoCompilationRepository;



    @Resource
    private MinioService minioService;


    /**
     * 视频分页
     *
     * @param videoPageDTO
     * @return
     */
    @Override
    public PageData queryVideoPage(VideoPageDTO videoPageDTO) {
        videoPageDTO.setUserId(UserContext.getUserId());
        videoPageDTO.setPageNum((videoPageDTO.getPageNum() - 1) * videoPageDTO.getPageSize());

        Pageable pageable = PageRequest.of((videoPageDTO.getPageNum() -1 ), videoPageDTO.getPageSize());

        Page<Video> videoList = videoMapper.selectVideoPage(
                videoPageDTO.getUserId(),
                videoPageDTO.getVideoTitle(),
                videoPageDTO.getPublishType(),
                videoPageDTO.getShowType(),
                videoPageDTO.getPositionFlag(),
                videoPageDTO.getAuditsStatus()
        ,  pageable);
        if (videoList.isEmpty()) {
            return PageData.emptyPage();
        }
        return PageData.genPageData(videoList.getContent(), videoMapper.selectVideoPageCount(videoPageDTO.getUserId(),
                videoPageDTO.getVideoTitle(),
                videoPageDTO.getPublishType(),
                videoPageDTO.getShowType(),
                videoPageDTO.getPositionFlag(),
                videoPageDTO.getAuditsStatus()));

    }

    /**
     * 视频合集分页
     *
     * @param videoCompilationPageDTO
     * @return
     */
    @Override
    public PageData queryVideoCompilationPage(videoCompilationPageDTO videoCompilationPageDTO) {
        videoCompilationPageDTO.setUserId(UserContext.getUserId());
        videoCompilationPageDTO.setPageNum((videoCompilationPageDTO.getPageNum() - 1) * videoCompilationPageDTO.getPageSize());

        Pageable pageable = PageRequest.of((videoCompilationPageDTO.getPageNum() -1 ), videoCompilationPageDTO.getPageSize());
        Page<Video> videosPage = videoMapper.findAllByUserId(videoCompilationPageDTO.getUserId(), pageable);

        Page<UserVideoCompilation> compilationList = userVideoCompilationRepository.selectVideoCompilationPage(
                videoCompilationPageDTO.getUserId(),
                videoCompilationPageDTO.getTitle(),
                pageable
        );
        if (compilationList.isEmpty()) {
            return PageData.emptyPage();
        }
        return PageData.genPageData(compilationList.getContent(), userVideoCompilationRepository.selectVideoCompilationPageCount(videoCompilationPageDTO.getUserId()
        , videoCompilationPageDTO.getTitle()));
    }

    /**
     * 上传图文视频图片
     *
     * @param file
     * @return
     */
    @Override
    public String uploadVideoImage(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFilename)) {
            throw new CustomException(HttpCodeEnum.IMAGE_TYPE_FOLLOW);
        }
        // todo 对文件大小进行判断
        // 对原始文件名进行判断
        if (originalFilename.endsWith(".png")
                || originalFilename.endsWith(".jpg")
                || originalFilename.endsWith(".jpeg")
                || originalFilename.endsWith(".webp")) {
            return minioService.uploadFile(file);
        } else {
            throw new CustomException(HttpCodeEnum.IMAGE_TYPE_FOLLOW);
        }
    }

    /**
     * 上传视频
     *
     * @param file
     * @return
     */
    @Override
    public String uploadVideo(MultipartFile file) throws Exception {
        return minioService.uploadFile(file);
    }

    /**
     * 分片上传视频
     *
     * @param file
     * @return
     */
    @Override
    public String multipartUploadVideo(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioService.multipartUploadVideoFile(file);
    }

    /**
     * 视频播放量
     */
    @Override
    public DashboardAmountVO dashboardAmount() {
        // todo 添加每日10点的定时任务缓存到redis

        Long userId = UserContext.getUserId();
        DashboardAmountVO dashboardAmountVO = new DashboardAmountVO();
        Long videoPlayCount = videoMapper.selectVideoPlayAmount(userId);
        dashboardAmountVO.setPlayAmount(new DashboardAmountItem(videoPlayCount, videoMapper.selectVideoPlayAmountAdd(userId), videoMapper.selectVideoPlayAmount7Day(userId)));
        dashboardAmountVO.setIndexViewAmount(new DashboardAmountItem(userId, userId));
        dashboardAmountVO.setFansAmount(new DashboardAmountItem(videoMapper.selectFansAmount(userId), videoMapper.selectFansAmountAdd(userId), videoMapper.selectFansAmount7Day(userId)));
        dashboardAmountVO.setLikeAmount(new DashboardAmountItem(videoMapper.selectVideoLikeAmount(userId), videoMapper.selectVideoLikeAmountAdd(userId), videoMapper.selectVideoLikeAmount7Day(userId)));
        dashboardAmountVO.setCommentAmount(new DashboardAmountItem(videoMapper.selectVideoCommentAmount(userId), videoMapper.selectVideoCommentAmountAdd(userId), videoMapper.selectVideoCommentAmount7Day(userId)));
        dashboardAmountVO.setShareAmount(new DashboardAmountItem(1L, 0L));
        return dashboardAmountVO;
    }

}
