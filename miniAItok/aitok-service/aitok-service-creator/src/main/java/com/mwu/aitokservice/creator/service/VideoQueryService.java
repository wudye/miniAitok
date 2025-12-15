package com.mwu.aitokservice.creator.service;

import com.mwu.aitok.model.creator.dto.VideoPageDTO;
import com.mwu.aitok.model.creator.dto.videoCompilationPageDTO;
import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitokservice.creator.repository.UserVideoCompilationRepository;
import com.mwu.aitokservice.creator.repository.VideoRepository;
import com.mwu.aitokservice.creator.repository.VideoSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 视频查询服务
 * 展示 JPA 分页查询的使用方法
 */
@Service
public class VideoQueryService {
    
    @Autowired
    private VideoRepository videoRepository;
    
    @Autowired
    private UserVideoCompilationRepository userVideoCompilationRepository;
    
    // ==================== 视频分页查询 ====================
    
    /**
     * 分页查询视频
     */
    public Page<Video> selectVideoPage(VideoPageDTO videoPageDTO) {
        // 创建分页对象 (注意：Spring Data JPA 的页码从 0 开始)
        Pageable pageable = PageRequest.of(
            videoPageDTO.getPageNum() - 1,  // 转换为 0-based 页码
            videoPageDTO.getPageSize()
        );
        Specification<Video> spec = VideoSpecification.countQuery(
                videoPageDTO.getUserId(),
                videoPageDTO.getVideoTitle(),
                videoPageDTO.getPublishType(),
                videoPageDTO.getShowType(),
                videoPageDTO.getPositionFlag(),
                videoPageDTO.getAuditsStatus()
        );
        Page<Video> videos = videoRepository.findAll(spec, pageable);
        return  videos;

        

    }
    
    /**
     * 统计视频数量
     */
    public long selectVideoPageCount(VideoPageDTO videoPageDTO) {
        return videoRepository.selectVideoPageCount(
            videoPageDTO.getUserId(),
            videoPageDTO.getVideoTitle(),
            videoPageDTO.getPublishType(),
            videoPageDTO.getShowType(),
            videoPageDTO.getPositionFlag(),
            videoPageDTO.getAuditsStatus()
        );
    }
    
    // ==================== 视频合集分页查询 ====================
    
    /**
     * 分页查询视频合集
     */
    public Page<UserVideoCompilation> selectVideoCompilationPage(videoCompilationPageDTO dto) {
        Pageable pageable = PageRequest.of(
            dto.getPageNum() - 1,  // 转换为 0-based 页码
            dto.getPageSize()
        );
        
        return userVideoCompilationRepository.selectVideoCompilationPage(
            dto.getUserId(),
            dto.getTitle(),
            pageable
        );
    }
    
    /**
     * 统计视频合集数量
     */
    public long selectVideoCompilationPageCount(videoCompilationPageDTO dto) {
        return userVideoCompilationRepository.selectVideoCompilationPageCount(
            dto.getUserId(),
            dto.getTitle()
        );
    }
    
    // ==================== 简化的查询方法 ====================
    
    /**
     * 简化的视频分页查询 - 使用方法名查询
     */
    public Page<Video> getVideosByUserId(Long userId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return videoRepository.findAllByUserId(userId, pageable);
    }
    
    /**
     * 按标题模糊查询视频
     */
    public Page<Video> searchVideosByTitle(Long userId, String title, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return videoRepository.selectVideoPage(
            userId, 
            title, 
            null, null, null, null, 
            pageable
        );
    }
    
    /**
     * 按发布类型查询视频
     */
    public Page<Video> getVideosByPublishType(Long userId, String publishType, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        return videoRepository.selectVideoPage(
            userId, 
            null, 
            publishType, null, null, null, 
            pageable
        );
    }
}