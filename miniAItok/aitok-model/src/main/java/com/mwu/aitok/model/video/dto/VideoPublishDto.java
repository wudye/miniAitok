package com.mwu.aitok.model.video.dto;


import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.domain.VideoPosition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 功能：
 * 作者：lzq
 * 日期：2023/10/29 14:58
 */
@Data
public class VideoPublishDto  {

    // 分类名称
    private Long categoryId;

    // 视频标签
    private Long[] videoTags;

    // 图文的图片集合
    private String[] imageFileList;

    // 视频定位
    private VideoPosition position;

    // 视频合集id
    private Long compilationId;

    private Video video;

    public VideoPublishDto( ) {

    }
    public VideoPublishDto(Video video) {
        this.video = video;
    }

    public static VideoPublishDto from(Video video) {
        return new VideoPublishDto(video);
    }


}
