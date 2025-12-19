package com.mwu.aitok.service.video.service;

import com.mwu.aitok.model.video.domain.VideoTag;

import java.util.List;

public interface IVideoTagRelationService {


    /**
     * 根据视频id和标签id数组批量插入
     */
    boolean saveVideoTagRelationBatch(String videoId, Long[] tagIds);

    /**
     * 查询视频标签
     *
     * @param videoId 视频id
     * @return String[]
     */
    String[] queryVideoTags(String videoId);

    /**
     * 获取视频的标签集合
     *
     * @param videoId
     * @return
     */
    List<String> queryVideoTagsReturnList(String videoId);

    /**
     * 根据视频id获取标签集合
     *
     * @param videoId
     * @return
     */
    List<VideoTag> queryVideoTagsByVideoId(String videoId);

    /**
     * 根据视频id获取标签ids
     *
     * @param videoId
     * @return
     */
    List<Long> queryVideoTagIdsByVideoId(String videoId);

    /**
     * 删除视频关联标签
     */
    boolean deleteRecordByVideoId(String videoId);
}
