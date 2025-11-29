package com.mwu.atiokservice.behave.controller.v1;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.behave.domain.VideoUserLike;
import com.mwu.aitok.model.video.dto.VideoPageDto;
import com.mwu.aitolk.feign.video.RemoteVideoService;
import com.mwu.atiokservice.behave.repository.VideoUserLikeRepository;
import com.mwu.atiokservice.behave.service.IVideoUserLikeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞表(VideoUserLike)表控制层
 *
 * @author mwu
 * @since 2023-10-30 14:32:56
 */
@RestController
@RequestMapping("/api/v1/like")
public class VideoUserLikeController {

    @Resource
    private IVideoUserLikeService videoUserLikeService;

    @Resource
    private VideoUserLikeRepository videoUserLikeRepository;

    @Resource
    private RemoteVideoService remoteVideoService;

    /**
     * 用户点赞
     */
    @GetMapping("/{videoId}")
    public R<Boolean> getDetails(@PathVariable("videoId") String videoId) throws JsonProcessingException {
        return R.ok(videoUserLikeService.videoLike(videoId));
    }

    /**
     * todo 使用sql 用户点赞分页查询
     */
    @PostMapping("/mylikepage")
    public PageData myLikePage(@RequestBody VideoPageDto pageDto) {
        return videoUserLikeService.queryMyLikeVideoPage(pageDto);
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/{videoId}")
    public R<?> deleteVideoLikeRecord(@PathVariable("videoId") String videoId) {

        videoUserLikeRepository.deleteByVideoId(videoId);
        return R.ok("success");
    }

    /**
     * 用户是否点赞某视频
     *
     * @param videoId
     * @return
     */
    @GetMapping("/weather/{videoId}")
    public R<Boolean> weatherLike(@PathVariable("videoId") String videoId) {

        return R.ok(videoUserLikeRepository.countByVideoIdAndUserId(videoId, UserContext.getUserId()) > 0);
    }

    /**
     * 我的喜欢数
     */
    @GetMapping("/likeCount")
    public R<Long> countFavorite() {
        return R.ok(videoUserLikeRepository.countByUserId(UserContext.getUserId()));
    }

    /**
     * 分页查询用户的点赞列表
     *
     * @param pageDto
     * @return
     */
    @PostMapping("/personLikePage")
    public PageData personLikePage(@RequestBody VideoPageDto pageDto) {
        return videoUserLikeService.queryPersonLikePage(pageDto);
    }

}

