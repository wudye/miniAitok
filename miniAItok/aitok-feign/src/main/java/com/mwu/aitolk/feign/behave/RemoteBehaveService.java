package com.mwu.aitolk.feign.behave;


import com.mwu.aitiokcoomon.core.constant.ServiceNameConstants;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitolk.feign.behave.fallback.RemoteBehaveServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * RemoteBehaveService
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/4
 **/
@FeignClient(contextId = "remoteBehaveService", value = ServiceNameConstants.BEHAVE_SERVICE, fallbackFactory = RemoteBehaveServiceFallback.class)
public interface RemoteBehaveService {

    /**
     * 获取指定视频评论量
     *
     * @param videoId
     * @return
     */
    @GetMapping("/api/v1/comment/{videoId}")
    R<Long> getCommentCountByVideoId(@PathVariable("videoId") String videoId);

    @DeleteMapping("/api/v1/video/{videoId}")
    R<?> deleteVideoDocumentByVideoId(@PathVariable("videoId") String videoId);

    @DeleteMapping("/{videoId}")
    R<?> deleteVideoLikeRecord(@PathVariable String videoId);

    @DeleteMapping("/{videoId}")
    R<?> deleteVideoFavoriteRecordByVideoId(@PathVariable String videoId);

    /**
     * 是否点赞某视频
     *
     * @param videoId
     * @return
     */
    @GetMapping("/api/v1/like/weather/{videoId}")
    R<Boolean> weatherLike(@PathVariable("videoId") String videoId);

    /**
     * 是否收藏某视频
     *
     * @param videoId
     * @return
     */
    @GetMapping("/api/v1/favorite/weather/{videoId}")
    R<Boolean> weatherFavorite(@PathVariable("videoId") String videoId);

    @GetMapping("/api/v1/app/like/likeNum/{videoId}")
    R<Long> getLikeNumByVideoId(Long videoId);


    @GetMapping("/api/v1/app/like/count/{videoId}/{userId}")
    Integer countByVideoIdAndUserId(@PathVariable long videoId, @PathVariable long userId);


    @GetMapping("/api/v1/app/favorite/count/{videoId}/{userId}")
    long countByVideoIdAndUserId(@PathVariable String videoId, @PathVariable long userId);

    @GetMapping("/countRemote/{userId}")
    public R<Long> selectVideoCommentAmount(@PathVariable("userId") Long userId) ;
    @GetMapping("/countRemoteAdd/{userId}")
    public R<Long> selectVideoCommentAmountAdd(@PathVariable("userId") Long userId) ;
}

