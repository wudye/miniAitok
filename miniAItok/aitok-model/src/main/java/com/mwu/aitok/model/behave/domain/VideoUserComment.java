package com.mwu.aitok.model.behave.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * (VideoUserComment)实体类
 *
 * @author mwu
 * @since 2023-10-30 16:52:51
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "video_user_comment")
public class VideoUserComment implements Serializable {
    private static final long serialVersionUID = -19005250815450923L;
    /**
     * 评论id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
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
    @NotBlank
    @Size(max = 300, message = "评论内容不能超过300字符")
    private String content;
    /**
     * 点赞量
     */
    private Long likeNum;
    /**
     * 状态：0默认1禁止
     */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}

