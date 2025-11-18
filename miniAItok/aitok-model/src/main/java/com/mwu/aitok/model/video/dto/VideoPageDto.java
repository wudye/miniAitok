package com.mwu.aitok.model.video.dto;

import com.mwu.aitok.model.video.domain.Video;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 功能：
 * 作者：lzq
 * 日期：2023/10/29 19:55
 */
@Data
public class VideoPageDto  {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Video video;

    public VideoPageDto() {
    }

    public VideoPageDto(Video video) {
        this.video = video;
    }


    public static VideoPageDto from(Video video) {
        return new VideoPageDto(video);
    }
}

