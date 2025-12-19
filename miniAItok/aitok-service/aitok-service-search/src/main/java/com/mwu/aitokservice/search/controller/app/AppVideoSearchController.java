package com.mwu.aitokservice.search.controller.app;



import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.search.dto.PageDTO;
import com.mwu.aitok.model.search.dto.VideoSearchKeywordDTO;
import com.mwu.aitokservice.search.service.VideoSearchService;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * VideoSearchController
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/31
 **/
@RestController
@RequestMapping("/api/v1/app/video")
public class AppVideoSearchController {

    @Resource
    private VideoSearchService videoSearchService;

    /**
     * 分页搜索视频
     */
    @PostMapping()
    public PageData<?> searchVideoForApp(@RequestBody VideoSearchKeywordDTO dto) {
        return videoSearchService.searchVideoFromESForApp(dto);
    }

    /**
     * 牛音热搜
     */
    @PostMapping("/hotSearch")
    @Cacheable(value = "hotSearch", key = "'hotSearch' + #pageDTO.pageNum + '_' + #pageDTO.pageSize")
    public R<?> getHotSearchForApp(@RequestBody PageDTO pageDTO) {
        return R.ok(videoSearchService.findSearchHot(pageDTO));
    }

}
