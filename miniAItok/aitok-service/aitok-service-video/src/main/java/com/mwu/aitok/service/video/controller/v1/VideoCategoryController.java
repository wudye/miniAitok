package com.mwu.aitok.service.video.controller.v1;

import com.mwu.aitiokcoomon.core.annotations.MappingCostTime;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.video.dto.VideoCategoryPageDTO;
import com.mwu.aitok.model.video.vo.VideoCategoryVo;
import com.mwu.aitok.model.video.vo.app.AppVideoCategoryVo;
import com.mwu.aitok.service.video.service.IVideoCategoryService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * (VideoCategory)表控制层
 *
 * @author lzq
 * @since 2023-10-30 19:41:13
 */
@RestController
@RequestMapping("/api/v1/category")
public class VideoCategoryController {

    @Resource
    private IVideoCategoryService videoCategoryService;

    @GetMapping()
    public R<?> getAllParentCategory() {
        List<VideoCategoryVo> categoryNames = videoCategoryService.selectAllParentCategory();
        return R.ok(categoryNames);
    }

    @PostMapping("/page")
    public PageData<?> categoryVideoPage(@RequestBody VideoCategoryPageDTO pageDTO) {
        return videoCategoryService.selectVideoByCategory(pageDTO);
    }

    /**
     * 根据分类推送10条视频
     */
    @GetMapping("/pushVideo/{categoryId}")
    public R<?> categoryVideoPage(@PathVariable Long categoryId) {
        return R.ok(videoCategoryService.pushVideoByCategory(categoryId));
    }

    /**
     * 返回视频分类树形结构
     */
    @GetMapping("/tree")
    public R<?> getCategoryTree() {
        return R.ok(videoCategoryService.getCategoryTree());
    }

    /**
     * 获取视频父分类
     */
    @MappingCostTime
    @GetMapping("/parentList")
    public R<?> getVideoParentCategoryList() {
        return R.ok(videoCategoryService.getVideoParentCategoryList());
    }

    /**
     * 获取视频父分类的子分类
     */
    @MappingCostTime
    @GetMapping("/children/{id}")
    public R<List<AppVideoCategoryVo>> getParentCategoryChildrenList(@PathVariable("id") Long id) {
        return R.ok(videoCategoryService.getNormalChildrenCategory(id));
    }

}

