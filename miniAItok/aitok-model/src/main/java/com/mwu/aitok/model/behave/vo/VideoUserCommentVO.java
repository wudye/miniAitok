package com.mwu.aitok.model.behave.vo;

import com.mwu.aitok.model.behave.domain.VideoUserComment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * VideoUserCommentVO
 **/
@NoArgsConstructor
@Data
public class VideoUserCommentVO {
    private VideoUserComment parentComment;
    // 评论者昵称
    private String nickName;
    // 评论者头像
    private String avatar;

    // 被回复者id
    private Long replayUserId;
    // 被回复者昵称
    private String replayUserNickName;

    // 子评论，默认二级
    private List<VideoUserCommentVO> children;

    public VideoUserCommentVO(VideoUserComment videoUserComment) {
        this.parentComment  = videoUserComment;
    }

    public static VideoUserCommentVO from(VideoUserComment videoUserComment) {
        return new VideoUserCommentVO(videoUserComment);
    }
}
