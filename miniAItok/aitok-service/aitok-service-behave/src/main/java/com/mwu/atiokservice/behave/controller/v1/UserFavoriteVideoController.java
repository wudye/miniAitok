package com.mwu.atiokservice.behave.controller.v1;

import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.behave.domain.UserFavoriteVideo;
import com.mwu.aitok.model.behave.dto.UserFavoriteVideoDTO;
import com.mwu.atiokservice.behave.service.IUserFavoriteVideoService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * (UserFavoriteVideo)表控制层
 *
 * @author lzq
 * @since 2023-11-17 10:16:03
 */
@RestController
@RequestMapping("/api/v1/userFavoriteVideo")
public class UserFavoriteVideoController {

    @Resource
    private IUserFavoriteVideoService userFavoriteVideoService;

    /**
     * 收藏视频
     */
    @PostMapping()
    public R<Boolean> favoriteVideoToCollection(@Validated @RequestBody UserFavoriteVideoDTO userFavoriteVideoDTO) {
        return R.ok(userFavoriteVideoService.videoFavorites(userFavoriteVideoDTO));
    }

    /**
     * 根据视频id查询被哪些收藏夹收藏
     */
    @GetMapping("/{videoId}")
    public R<Long[]> getVideoUserCollection(@PathVariable("videoId") String videoId) {
        Long userId = UserContext.getUserId();
        List<UserFavoriteVideo> list = userFavoriteVideoService.getUserFavoriteVideos(userId, videoId);
        Long[] array = list.stream().map(UserFavoriteVideo::getFavoriteId).toArray(Long[]::new);
        return R.ok(array);
    }

}

