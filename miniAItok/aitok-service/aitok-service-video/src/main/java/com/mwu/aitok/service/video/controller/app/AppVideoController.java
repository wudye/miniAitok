package com.mwu.aitok.service.video.controller.app;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitok.model.video.dto.VideoPageDto;
import com.mwu.aitok.model.video.vo.app.VideoInfoVO;
import com.mwu.aitok.model.video.vo.app.VideoRecommendVO;
import com.mwu.aitok.service.video.service.IVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;




/*

功能
IPage (MyBatis-Plus)
Page (Spring Data)
当前页数据列表
getRecords()
getContent()
总记录数
getTotal()
getTotalElements()
当前页码
getCurrent()
getNumber()
每页记录数
getSize()
getSize()
总页数
getPages()
getTotalPages()
是否有下一页
hasNext()
hasNext()
是否有上一页
hasPrevious()
hasPrevious()
结论
两者功能类似，但字段命名不同。
Spring Page 的页码从 0 开始，而 IPage 从 1 开始。
如果需要从 IPage 转换为 Page，可以手动映射字段。
 */
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
public class AppVideoController {

    private final IVideoService videoService;

    /**
     * 首页推送视频
     */
    @GetMapping("/recommend")
    public R<List<VideoRecommendVO>> appPushVideo() {

        System.out.println("AppVideoController appPushVideo called");
        return R.ok(videoService.pushAppVideoList());
    }



    /**
     * 视频详情
     */
    @GetMapping("/info/{videoId}")
    public R<VideoInfoVO> appVideoInfo(@PathVariable("videoId") String videoId) {
        return R.ok(videoService.getVideoInfoForApp(videoId));
    }

    /**
     * 热门视频分页
     */
    @PostMapping("/hotVideo")
    @Cacheable(value = "hotVideos", key = "'hotVideos'+ #pageDTO.pageNum + '_' + #pageDTO.pageSize")
    public PageData<?> hotVideosForApp(@RequestBody PageDTO pageDTO) {
        return videoService.getHotVideos(pageDTO);
    }

    /**
     * 分页查询我的视频
     */
    @PostMapping("/myPage")
    public PageData<?> myPageForApp(@RequestBody VideoPageDto pageDto) {
        return videoService.queryMyVideoPageForApp(pageDto);
    }

    /**
     * 分页查询用户视频
     */
    @PostMapping("/userPage")
    public PageData<?> userPage(@RequestBody VideoPageDto pageDto) {
        return videoService.queryUserVideoPage(pageDto);
    }
}
