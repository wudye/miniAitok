package com.mwu.aitok.service.video.service;

import com.mwu.aitok.model.video.domain.VideoImage;

import java.util.List;

public interface IVideoImageService {


    /**
     * 通过视频id查询视频图片
     *
     * @param videoId
     * @return
     */
    List<VideoImage> queryImagesByVideoId(String videoId);

    /**
     * 删除视频
     *
     * @param videoId
     * @return
     */
    boolean deleteVideoImagesByVideoId(String videoId);

    void save(VideoImage videoImage);
}
