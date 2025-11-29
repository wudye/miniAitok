package com.mwu.atiokservice.behave.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.behave.domain.VideoUserFavorites;
import com.mwu.aitok.model.video.dto.VideoPageDto;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 视频收藏表(VideoUserFavorites)表服务接口
 *
 * @author mwu
 * @since 2023-10-31 15:57:38
 */
public interface IVideoUserFavoritesService  {

    /**
     * 收藏视频
     */
    boolean userOnlyFavoriteVideo(String videoId) throws JsonProcessingException;

    /**
     * 取消收藏视频
     */
    boolean userUnFavoriteVideo(String videoId);

    Page<VideoUserFavorites> queryFavoritePage(VideoPageDto pageDto);

    /**
     * 分页查询用户收藏的视频
     *
     * @param pageDto
     * @return
     */
    PageData queryUserFavoriteVideoPage(VideoPageDto pageDto);

    PageData queryMyFavoriteVideoPageForApp(VideoPageDto pageDto);

    PageData queryUserFavoriteVideoPageForApp(VideoPageDto pageDto);

    /**
     * 删除说有用户收藏此视频记录 ！！！
     *
     * @param videoId
     * @return
     */
    boolean removeFavoriteRecordByVideoId(String videoId);

    Long getFavoriteCountByVideoId(String videoId);

    /**
     * 获取用户收藏视频列表
     *
     * @param userId
     * @return
     */
    List<String> getFavoriteVideoIdListByUserId(Long userId);

    /**
     * 是否收藏视频
     *
     * @param videoId
     * @param userId
     * @return
     */
    boolean weatherFavoriteVideo(String videoId, Long userId);
}
