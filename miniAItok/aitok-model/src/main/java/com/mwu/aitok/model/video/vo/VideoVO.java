package com.mwu.aitok.model.video.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.domain.VideoPosition;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * VideoVO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/31
 **/
@NoArgsConstructor
@Data
public class VideoVO  {

    private Long commentNum;
    private String userNickName;
    private String userAvatar;
    // 是否点赞
    private boolean weatherLike;
    // 是否收藏
    private boolean weatherFavorite;
    // 是否关注
    private boolean weatherFollow;
    // 标签数组
    private String[] tags;
    // 图片集合
    private String[] imageList;
    // 位置信息
    private VideoPosition position;
    // 视频所在视频合集
    @JsonIgnore
    private UserVideoCompilationInfoVO userVideoCompilationInfoVO;

    // 热力值
    private Double hotScore;

    private Video video;

    public VideoVO(Video video) {
        this.video = video;

    }

    public static VideoVO build(Video video) {
        return new VideoVO(video);
    }

}
