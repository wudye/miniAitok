package com.mwu.aitok.service.video.service;


import com.mwu.aitok.model.video.domain.VideoCategoryRelation;

import java.util.List;

/**
 * 视频分类关联表(VideoCategoryRelation)表服务接口
 *
 * @author lzq
 * @since 2023-10-31 14:44:35
 */
public interface IVideoCategoryRelationService  {

    boolean saveVideoCategoryRelation(VideoCategoryRelation videoCategoryRelation);

    /**
     * 根据视频id查询分类ids
     *
     * @param videoId
     * @return
     */
    List<Long> queryVideoCategoryIdsByVideoId(String videoId);

    /**
     * 删除视频分类关联
     *
     * @param videoId
     * @return
     */
    boolean deleteRecordByVideoId(String videoId);
}
