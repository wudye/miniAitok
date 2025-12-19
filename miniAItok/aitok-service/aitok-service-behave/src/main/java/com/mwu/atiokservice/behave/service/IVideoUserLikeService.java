package com.mwu.atiokservice.behave.service;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.behave.domain.VideoUserLike;
import com.mwu.aitok.model.video.dto.VideoPageDto;

import java.util.List;

/**
 * 点赞表(VideoUserLike)表服务接口
 *
 * @author mwu
 * @since 2023-10-30 14:33:00
 */
public interface IVideoUserLikeService {

    boolean videoLike(String videoId) throws JsonProcessingException;

    /**
     * 分页查询我的点赞视频
     *
     * @param pageDto
     * @return
     */
    PageData queryMyLikeVideoPage(VideoPageDto pageDto);

    PageData queryMyLikeVideoPageForApp(VideoPageDto pageDto);

    /**
     * 查询用户的点赞列表
     *
     * @param pageDto
     * @return
     */
    PageData queryPersonLikePage(VideoPageDto pageDto);

    /**
     * 删除所有用户对此视频的点赞 ！！！
     *
     * @param videoId
     * @return
     */
    boolean removeLikeRecordByVideoId(String videoId);

    /**
     * 获取视频点赞数
     *
     * @param videoId
     * @return
     */
    Long getVideoLikeNum(String videoId);

    /**
     * 获取用户点赞视频id列表
     */
    List<String> getVideoIdsByUserId(Long userId);

    /**
     * 视频点赞
     */
    Boolean videoActionLike(String videoId) throws JsonProcessingException;

    Boolean videoActionUnlike(String videoId);

    boolean weatherLikeVideo(String videoId, Long userId);
}
