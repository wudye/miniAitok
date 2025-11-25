package com.mwu.aitokservice.search.controller.v1;

import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.*;

import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.search.dto.PageDTO;
import com.mwu.aitok.model.search.dto.VideoSearchKeywordDTO;
import com.mwu.aitok.model.search.dto.VideoSearchSuggestDTO;
import com.mwu.aitok.model.search.vo.VideoSearchVO;
import com.mwu.aitokservice.search.domain.vo.VideoSearchUserVO;
import com.mwu.aitokservice.search.service.VideoSearchService;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VideoSearchController
 *
 * @AUTHOR: roydon
 * @DATE: 2023/10/31
 **/
@RestController
@RequestMapping("/api/v1/video")
public class VideoSearchController {

    @Resource
    private VideoSearchService videoSearchService;

    @GrpcClient("aitok-video")
    private VideoServiceGrpc.VideoServiceBlockingStub videoServiceBlockingStub;

    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceBlockingStub;

    @GrpcClient("aitok-behave")
    private BehaveServiceGrpc.BehaveServiceBlockingStub behaveServiceBlockingStub;



    /**
     * 分页搜索视频
     */
    @PostMapping()
    public R<List<VideoSearchUserVO>> searchVideo(@RequestBody VideoSearchKeywordDTO dto) {
        List<VideoSearchVO> videoSearchVOS = videoSearchService.searchVideoFromES(dto);
        if (StringUtils.isNull(videoSearchVOS) || videoSearchVOS.isEmpty()) {
            return R.ok();
        }
        List<VideoSearchUserVO> res = BeanCopyUtils.copyBeanList(videoSearchVOS, VideoSearchUserVO.class);
        // 封装用户，视频点赞量，喜欢量。。。
        res.forEach(v -> {
            // 用户头像
            GetByIdRequest request = GetByIdRequest.newBuilder().setUserId(v.getUserId()).build();
            MemberResponse response = memberServiceBlockingStub.getById(request);
            Member member = BeanCopyUtils.copyBean(response, Member.class);
            v.setUserNickName(member.getNickName());
            v.setUserAvatar(member.getAvatar());
            // 图文视频
            VideoIdRequest imageRequest = VideoIdRequest.newBuilder().setVideoId(Long.parseLong(v.getVideoId())).build();
            VideoImagesListResponse imageResponse = videoServiceBlockingStub.apiGetVideoImagesByVideoId(imageRequest);
            v.setImageList(imageResponse.getVideoImagesListList());
            // 是否点赞、是否收藏
            ApiWeatherLikeVideoRequest likeRequest = ApiWeatherLikeVideoRequest.newBuilder()
                    .setVideoId(Long.parseLong(v.getVideoId()))
                    .setUserId(UserContext.getUserId())
                    .build();
            ApiWeatherLikeVideoResponse likeResponse = videoServiceBlockingStub.apiWeatherLikeVideo(likeRequest);
            v.setWeatherLike(likeResponse.getLiked());

            // 是否收藏
            ApiWeatherLikeVideoResponse likeResponse2 = videoServiceBlockingStub.apiWeatherFavoriteVideo(likeRequest);
            v.setWeatherFavorite(likeResponse2.getLiked());
            // 行为数据：点赞数、评论数、收藏数
            NumRequest numRequest = NumRequest.newBuilder().setVideoId(Long.parseLong(v.getVideoId())).build();
            NumResponse likeNumResponse = behaveServiceBlockingStub.apiGetVideoLikeNum(numRequest);
            v.setLikeNum(likeNumResponse.getNum());
            likeNumResponse = behaveServiceBlockingStub.apiGetVideoCommentNum(numRequest);
            v.setCommentNum(likeNumResponse.getNum());
            likeNumResponse = behaveServiceBlockingStub.apiGetVideoFavoriteNum(numRequest);
            v.setFavoritesNum(likeNumResponse.getNum());
            // todo 社交数据、是否关注用户
            v.setWeatherFollow(false);
        });
        return R.ok(res);
    }

    @DeleteMapping("/{videoId}")
    public R<?> deleteVideo(@PathVariable("videoId") String videoId) {
        videoSearchService.deleteVideoDoc(videoId);
        return R.ok();
    }

    /**
     * 牛音热搜
     */
    @PostMapping("/search/hot")
    @Cacheable(value = "hotSearch", key = "'hotSearch' + #pageDTO.pageNum + '_' + #pageDTO.pageSize")
    public R<?> getSearchHot(@RequestBody PageDTO pageDTO) {
        return R.ok(videoSearchService.findSearchHot(pageDTO));
    }

    /**
     * 视频搜索建议
     */
    @PostMapping("/suggest")
    public R<?> getVideoSearchSuggest(@RequestBody VideoSearchSuggestDTO videoSearchSuggestDTO) {
        return R.ok(videoSearchService.pushVideoSearchSuggest(videoSearchSuggestDTO));
    }
}
