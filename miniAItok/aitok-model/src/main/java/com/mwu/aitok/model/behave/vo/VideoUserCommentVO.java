package com.mwu.aitok.model.behave.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mwu.aitok.model.behave.domain.VideoUserComment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private Long commentId;
    /**
     * 视频id
     */
    @NotNull
    private String videoId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 父id
     */
    private Long parentId;
    /**
     * 评论的根id
     */
    private Long originId;
    /**
     * 评论内容
     */

    private String content;
    /**
     * 点赞量
     */
    private Long likeNum;
    /**
     * 状态：0默认1禁止
     */
    private String status;

    private LocalDateTime createTime;
}
