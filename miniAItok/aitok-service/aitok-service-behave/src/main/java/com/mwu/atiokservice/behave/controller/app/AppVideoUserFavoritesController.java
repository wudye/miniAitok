package com.mwu.atiokservice.behave.controller.app;


import com.mwu.aitiokcoomon.core.domain.vo.PageData;
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
@RequestMapping("/api/v1/app/favorite")
public class AppVideoUserFavoritesController {

    @Resource
    private IVideoUserFavoritesService videoUserFavoritesService;

    @Resource
    private VideoUserFavoritesRepository videoUserFavoritesRepository;

    /**
     * 分页我的收藏
     */
    @PostMapping("/myPage")
    public PageData myFavoritePageForApp(@RequestBody VideoPageDto pageDto) {
        return videoUserFavoritesService.queryMyFavoriteVideoPageForApp(pageDto);
    }

    /**
     * 分页用户收藏
     */
    @PostMapping("/userPage")
    public PageData userFavoritePage(@RequestBody VideoPageDto pageDto) {
        return videoUserFavoritesService.queryUserFavoriteVideoPageForApp(pageDto);
    }

    @GetMapping("/count/{videoId}/{userId}")
    long countByVideoIdAndUserId(@PathVariable String videoId, @PathVariable long userId) {
        return (long) videoUserFavoritesRepository.countVideoUserFavoritesByUserIdAndVideoId(userId, videoId);
    }

}

