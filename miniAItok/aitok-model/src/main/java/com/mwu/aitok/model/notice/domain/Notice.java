package com.mwu.aitok.model.notice.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知表(Notice)实体类
 *
 * @author mwu
 * @since 2023-11-08 16:21:44
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notice")
public class Notice implements Serializable {
    private static final long serialVersionUID = -60869533333103048L;
    /**
     * 通知id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;
    /**
     * 用户id
     */
    private Long operateUserId;
    // 被通知的用户
    private Long noticeUserId;
    private String videoId;
    private Long commentId;
    /**
     * 内容
     */
    private String content;
    private String remark;
    /**
     * 通知类型(0：点赞，1：关注，2：收藏、3:视频被评论，4：回复评论、5：赞了评论)
     */
    private String noticeType;
    /**
     * 接收标志(0：未读 1：已读)
     */
    private String receiveFlag;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}

