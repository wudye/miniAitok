package com.mwu.atiokservice.behave.service;


import com.mwu.aitok.model.behave.domain.UserFavoriteVideo;
import com.mwu.aitok.model.behave.dto.UserFavoriteVideoDTO;

import java.util.List;

/**
 * (UserFavoriteVideo)表服务接口
 *
 * @author mwu
 * @since 2023-11-17 10:16:09
 */
public interface IUserFavoriteVideoService  {

    /**
     * 用户收藏视频到收藏夹功能
     *
     * @param userFavoriteVideoDTO
     * @return
     */
    Boolean videoFavorites(UserFavoriteVideoDTO userFavoriteVideoDTO);

    /**
     * 视频是否在收藏夹中
     *
     * @param favoriteId
     * @param videoId
     * @return
     */
    Boolean videoWeatherInFavoriteFolder(Long favoriteId, String videoId);

    List<UserFavoriteVideo> getUserFavoriteVideos(Long userId, String videoId);
}
