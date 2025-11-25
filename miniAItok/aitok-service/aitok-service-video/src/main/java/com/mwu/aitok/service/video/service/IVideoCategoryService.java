package com.mwu.aitok.service.video.service;


import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.video.domain.VideoCategory;
import com.mwu.aitok.model.video.dto.CategoryVideoPageDTO;
import com.mwu.aitok.model.video.dto.VideoCategoryPageDTO;
import com.mwu.aitok.model.video.vo.VideoCategoryTree;
import com.mwu.aitok.model.video.vo.VideoCategoryVo;
import com.mwu.aitok.model.video.vo.VideoPushVO;
import com.mwu.aitok.model.video.vo.app.AppVideoCategoryVo;

import java.util.List;

/**
 * (VideoCategory)表服务接口
 *
 * @author lzq
 * @since 2023-10-30 19:41:14
 */
public interface IVideoCategoryService  {

    List<VideoCategory> saveVideoCategoriesToRedis();

    List<VideoCategoryVo> selectAllCategory();
    List<VideoCategoryVo> selectAllParentCategory();

    /**
     * 分页根据分类获取视频
     *
     * @param pageDTO
     * @return
     */
    PageData selectVideoByCategory(VideoCategoryPageDTO pageDTO);

    /**
     * 根据分类推送视频
     *
     * @param categoryId
     * @return
     */
    List<VideoPushVO> pushVideoByCategory(Long categoryId);

    /**
     * 获取视频分类树
     */
    List<VideoCategoryTree> getCategoryTree();

    /**
     * 获取所有可用视频父分类
     */
    List<AppVideoCategoryVo> getNormalParentCategory();

    /**
     * 获取一级子分类
     *
     * @param id
     * @return
     */
    List<AppVideoCategoryVo> getNormalChildrenCategory(Long id);

    /**
     * 根据分类id分页获取视频
     *
     * @param pageDTO
     * @return
     */
    PageData getVideoPageByCategoryId(CategoryVideoPageDTO pageDTO);

    /**
     * 获取视频父分类集合
     *
     * @return
     */
    List<VideoCategory> getVideoParentCategoryList();

}
