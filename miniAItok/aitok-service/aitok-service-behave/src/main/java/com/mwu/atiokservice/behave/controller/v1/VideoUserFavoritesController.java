package com.mwu.atiokservice.behave.controller.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.behave.domain.VideoUserFavorites;
import com.mwu.aitok.model.video.dto.VideoPageDto;
import com.mwu.atiokservice.behave.repository.VideoUserFavoritesRepository;
import com.mwu.atiokservice.behave.service.IVideoUserFavoritesService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 视频收藏表(VideoUserFavorites)表控制层
 *
 * @author mwu
 * @since 2023-10-31 15:57:37
 */
@RestController
@RequestMapping("/api/v1/favorite")
public class VideoUserFavoritesController {

    @Resource
    private IVideoUserFavoritesService videoUserFavoritesService;

    @Resource
    private VideoUserFavoritesRepository videoUserFavoritesRepository;

    /**
     * 用户仅收藏视频
     */
    @GetMapping("/{videoId}")
    public R<Boolean> userFavoriteOnlyVideo(@PathVariable("videoId") String videoId) throws JsonProcessingException {
        return R.ok(videoUserFavoritesService.userOnlyFavoriteVideo(videoId));
    }

    /**
     * 取消收藏视频
     */
    @PutMapping("/unFavorite/{videoId}")
    public R<Boolean> userUnFavoriteVideo(@PathVariable("videoId") String videoId) {
        return R.ok(videoUserFavoritesService.userUnFavoriteVideo(videoId));
    }

    /**
     * 分页我的收藏
     *
     * @param pageDto
     * @return
     */
    @PostMapping("/mypage")
    public PageData myFavoritePage(@RequestBody VideoPageDto pageDto) {
        return videoUserFavoritesService.queryUserFavoriteVideoPage(pageDto);
    }

    @DeleteMapping("/{videoId}")
    public R<?> deleteVideoFavoriteRecordByVideoId(@PathVariable String videoId) {
       ;
        videoUserFavoritesRepository.deleteByVideoId(videoId);

        return R.ok("删除成功");
    }

    /**
     * 用户是否收藏某视频
     *
     * @param videoId
     * @return
     */
    @GetMapping("/weather/{videoId}")
    public R<Boolean> weatherFavorite(@PathVariable("videoId") String videoId) {


        return R.ok(videoUserFavoritesRepository.countVideoUserFavoritesByUserIdAndVideoId( UserContext.getUserId(), videoId) > 0);
    }

    /**
     * 我的作品收藏数
     */
    @GetMapping("/favoriteCount")
    public R<Long> countFavorite() {
        return R.ok(videoUserFavoritesRepository.countVideoUserFavoritesByUserId(UserContext.getUserId()));
    }
}

