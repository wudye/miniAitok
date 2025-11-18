package com.mwu.aitok.model.behave.vo.app;

import com.mwu.aitok.model.behave.domain.VideoUserComment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * VideoCommentReplayVO
 **/
@NoArgsConstructor
@Data
public class VideoCommentReplayVO {
    private VideoUserComment base;
    // 评论者昵称
    private String nickName;
    // 评论者头像
    private String avatar;

    // 被回复者id
    private Long replayUserId;
    // 被回复者昵称
    private String replayUserNickName;

    public VideoCommentReplayVO(VideoUserComment base) {
        this.base = base;
    }

    public static VideoCommentReplayVO from(VideoUserComment base) {
        return new VideoCommentReplayVO(base);
    }

}
