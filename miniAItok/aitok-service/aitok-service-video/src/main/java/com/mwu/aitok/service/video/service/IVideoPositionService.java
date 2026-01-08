package com.mwu.aitok.service.video.service;

import com.mwu.aitok.model.video.domain.VideoPosition;

public interface IVideoPositionService {
    /**
     * 通过视频id获取定位信息
     *
     * @param videoId
     * @return
     */
    VideoPosition queryPositionByVideoId(String videoId);

    /**
     * 删除视频定位
     *
     * @param videoId
     * @return
     */
    boolean deleteRecordByVideoId(String videoId);

    VideoPosition save(VideoPosition videoPosition);
}
