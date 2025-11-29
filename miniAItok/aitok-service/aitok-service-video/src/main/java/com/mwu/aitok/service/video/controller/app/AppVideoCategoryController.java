package com.mwu.aitok.service.video.controller.app;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.dto.CategoryVideoPageDTO;
import com.mwu.aitok.model.video.vo.app.AppVideoCategoryVo;
import com.mwu.aitok.service.video.service.IVideoCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * (VideoCategory)表控制层
 *
 * @author mwu
 * @since 2023-10-30 19:41:13
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/app/category")
public class AppVideoCategoryController {

    private final IVideoCategoryService videoCategoryService;

    /**
     * 获取所有可用视频父分类
     */
    @GetMapping("/parent")
    public R<List<AppVideoCategoryVo>> getNormalParentCategory() {
        return R.ok(videoCategoryService.getNormalParentCategory());
    }

    /**
     * 获取所有可用视频父分类
     */
    @GetMapping("/children/{id}")
    public R<List<AppVideoCategoryVo>> getNormalParentCategory(@PathVariable("id") Long id) {
        return R.ok(videoCategoryService.getNormalChildrenCategory(id));
    }

    /**
     * 分页分类视频
     */
    @PostMapping("/videoPage")
    public PageData<?> getVideoByCategoryId(@Validated @RequestBody CategoryVideoPageDTO pageDTO) {
        return videoCategoryService.getVideoPageByCategoryId(pageDTO);
    }



}

