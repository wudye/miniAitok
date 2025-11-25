package com.mwu.atiokservice.behave.controller.app;



import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.behave.domain.UserFavorite;
import com.mwu.atiokservice.behave.service.IUserFavoriteService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * (UserFavorite)表控制层
 *
 * @author lzq
 * @since 2023-11-13 16:37:53
 */
@RestController
@RequestMapping("/api/v1/app/userFavorite")
public class AppUserFavoriteController {

    @Resource
    private IUserFavoriteService userFavoriteService;

    /**
     * 我的收藏夹集合
     */
    @GetMapping("/list/{videoId}")
    public R<?> userFavoriteList(@PathVariable("videoId")String videoId) {
        return R.ok(userFavoriteService.userFavoritesFolderList(videoId));
    }

    /**
     * 新建收藏夹
     */
    @PostMapping()
    public R<?> newFavorite(@RequestBody UserFavorite userFavorite) {
        userFavorite.setUserId(UserContext.getUserId());
        return R.ok(userFavoriteService.saveFavorite(userFavorite));
    }

}

