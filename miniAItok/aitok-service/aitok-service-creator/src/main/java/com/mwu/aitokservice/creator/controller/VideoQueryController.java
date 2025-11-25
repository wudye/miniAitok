package com.mwu.aitokservice.creator.controller;

import com.mwu.aitok.model.creator.dto.VideoPageDTO;
import com.mwu.aitok.model.creator.dto.videoCompilationPageDTO;
import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitokservice.creator.service.VideoQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 视频查询控制器
 * 展示 JPA 分页查询的完整使用示例
 */
@RestController
@RequestMapping("/api/videos")
public class VideoQueryController {
    
    @Autowired
    private VideoQueryService videoQueryService;
    
    // ==================== 视频分页查询接口 ====================
    
    /**
     * 分页查询视频 - 完整参数版本
     */
    @PostMapping("/page")
    public ResponseEntity<Page<Video>> selectVideoPage(@RequestBody VideoPageDTO videoPageDTO) {
        Page<Video> result = videoQueryService.selectVideoPage(videoPageDTO);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 统计视频数量
     */
    @PostMapping("/count")
    public ResponseEntity<Long> selectVideoPageCount(@RequestBody VideoPageDTO videoPageDTO) {
        long count = videoQueryService.selectVideoPageCount(videoPageDTO);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 简化的视频分页查询 - GET 请求
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Video>> getVideosByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Video> result = videoQueryService.getVideosByUserId(userId, pageNum, pageSize);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 按标题搜索视频
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Video>> searchVideosByTitle(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Video> result = videoQueryService.searchVideosByTitle(userId, title, pageNum, pageSize);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 按发布类型查询视频
     */
    @GetMapping("/publish-type")
    public ResponseEntity<Page<Video>> getVideosByPublishType(
            @RequestParam Long userId,
            @RequestParam String publishType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<Video> result = videoQueryService.getVideosByPublishType(userId, publishType, pageNum, pageSize);
        return ResponseEntity.ok(result);
    }
    
    // ==================== 视频合集分页查询接口 ====================
    
    /**
     * 分页查询视频合集
     */
    @PostMapping("/compilations/page")
    public ResponseEntity<Page<UserVideoCompilation>> selectVideoCompilationPage(
            @RequestBody videoCompilationPageDTO dto) {
        Page<UserVideoCompilation> result = videoQueryService.selectVideoCompilationPage(dto);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 统计视频合集数量
     */
    @PostMapping("/compilations/count")
    public ResponseEntity<Long> selectVideoCompilationPageCount(@RequestBody videoCompilationPageDTO dto) {
        long count = videoQueryService.selectVideoCompilationPageCount(dto);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 简化的视频合集查询 - GET 请求
     */
    @GetMapping("/compilations/user/{userId}")
    public ResponseEntity<Page<UserVideoCompilation>> getVideoCompilationsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        videoCompilationPageDTO dto = new videoCompilationPageDTO();
        dto.setUserId(userId);
        dto.setTitle(title);
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        
        Page<UserVideoCompilation> result = videoQueryService.selectVideoCompilationPage(dto);
        return ResponseEntity.ok(result);
    }
}