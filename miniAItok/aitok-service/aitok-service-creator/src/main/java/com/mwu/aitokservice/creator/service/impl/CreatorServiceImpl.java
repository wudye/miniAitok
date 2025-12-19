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

import com.mwu.aitokservice.creator.repository.VideoSpecification;
import com.mwu.aitokservice.creator.service.CreatorService;
import com.mwu.aitokstarter.file.service.MinioService;
import com.mwu.aitolk.feign.behave.RemoteBehaveService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private RemoteBehaveService videoUserCommentRepository;


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

        Pageable pageable = PageRequest.of((videoPageDTO.getPageNum() -1 ), videoPageDTO.getPageSize());

        Specification<Video> spec = VideoSpecification.countQuery(
                videoPageDTO.getUserId(),
                videoPageDTO.getVideoTitle(),
                videoPageDTO.getPublishType(),
                videoPageDTO.getShowType(),
                videoPageDTO.getPositionFlag(),
                videoPageDTO.getAuditsStatus()
        );
        Page<Video> videos = videoMapper.findAll(spec, pageable);



        if (videos.isEmpty()) {
            return PageData.emptyPage();
        }
        return PageData.genPageData( videos.getContent(), videos.getTotalElements());

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

        return PageData.genPageData(compilationList.getContent(), compilationList.getTotalElements());
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
    public String multipartUploadVideo(MultipartFile file) throws Exception {
        return minioService.multipartUploadVideoFile(file);
    }

    /**
     * 视频播放量
     */
    @Override
    public DashboardAmountVO dashboardAmount() {
        // todo 添加每日10点的定时任务缓存到redis


        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication: " + authentication1.getPrincipal());


        Jwt jwt1 = ((JwtAuthenticationToken) authentication1).getToken();
        String userIda = jwt1.getClaim("userid");
        System.out.println("userId: " + userIda);
        Long userId = Long.parseLong(userIda);


        DashboardAmountVO dashboardAmountVO = new DashboardAmountVO();
        Long videoPlayCount = videoMapper.selectVideoPlayAmount(userId);
        System.out.println("videoPlayCount: " + videoPlayCount);
        dashboardAmountVO.setPlayAmount(new DashboardAmountItem(videoPlayCount, videoMapper.selectVideoPlayAmountAdd(userId), videoMapper.selectVideoPlayAmount7Day(userId)));
        dashboardAmountVO.setIndexViewAmount(new DashboardAmountItem(userId, userId));
        dashboardAmountVO.setFansAmount(new DashboardAmountItem(videoMapper.selectFansAmount(userId), videoMapper.selectFansAmountAdd(userId), videoMapper.selectFansAmount7Day(userId)));
        dashboardAmountVO.setLikeAmount(new DashboardAmountItem(videoMapper.selectVideoLikeAmount(userId), videoMapper.selectVideoLikeAmountAdd(userId), videoMapper.selectVideoLikeAmount7Day(userId)));

        Long videoFavoriteAmount = videoUserCommentRepository.selectVideoCommentAmount(userId).getData();
        dashboardAmountVO.setCommentAmount(new DashboardAmountItem(videoFavoriteAmount, videoUserCommentRepository.selectVideoCommentAmountAdd(userId).getData(), videoMapper.selectVideoCommentAmount7Day(userId)));
        dashboardAmountVO.setShareAmount(new DashboardAmountItem(1L, 0L));
        return dashboardAmountVO;
    }

}
