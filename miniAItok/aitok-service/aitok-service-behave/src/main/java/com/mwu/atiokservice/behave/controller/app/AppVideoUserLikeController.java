package com.mwu.atiokservice.behave.controller.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mwu.aitiokcoomon.core.domain.R;

import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.video.dto.VideoPageDto;
import com.mwu.atiokservice.behave.repository.VideoUserLikeRepository;
import com.mwu.atiokservice.behave.service.IVideoUserLikeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞表(VideoUserLike)表控制层
 *
 * @author lzq
 * @since 2023-10-30 14:32:56
 */
@RestController
@RequestMapping("/api/v1/app/like")
public class AppVideoUserLikeController {

    @Resource
    private IVideoUserLikeService videoUserLikeService;

    @Resource
    private VideoUserLikeRepository videoUserLikeRepository;

    /**
     * 用户点赞
     */
    @GetMapping("/{videoId}")
    public R<Boolean> userLikeAction(@PathVariable("videoId") String videoId) throws JsonProcessingException {
        return R.ok(videoUserLikeService.videoActionLike(videoId));
    }

    /**
     * 用户取消点赞
     */
    @GetMapping("/unlike/{videoId}")
    public R<Boolean> userUnlikeAction(@PathVariable("videoId") String videoId) {
        return R.ok(videoUserLikeService.videoActionUnlike(videoId));
    }

    /**
     * 我的点赞分页查询
     */
    @PostMapping("/myLikePage")
    public PageData myLikePageForApp(@RequestBody VideoPageDto pageDto) {
        return videoUserLikeService.queryMyLikeVideoPageForApp(pageDto);
    }

    /**
     * 分页用户点赞列表
     */
    @PostMapping("/userPage")
    public PageData personLikePage(@RequestBody VideoPageDto pageDto) {
        return videoUserLikeService.queryPersonLikePage(pageDto);
    }

    @GetMapping("/likeNum/{videoId}")
    R<Long> getLikeNumByVideoId(Long videoId) {
        Long likeNum = videoUserLikeRepository.getLikeNumByVideoId(String.valueOf(videoId));
        return R.ok(likeNum);
    }

    @GetMapping("/count/{videoId}/{userId}")
    Integer countByVideoIdAndUserId(@PathVariable long videoId, @PathVariable long userId) {
        return (int) videoUserLikeRepository.countByVideoIdAndUserId(String.valueOf(videoId), userId);
    }
}

