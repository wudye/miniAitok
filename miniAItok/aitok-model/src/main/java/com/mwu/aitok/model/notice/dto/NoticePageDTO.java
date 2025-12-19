package com.mwu.aitok.model.notice.dto;

import com.mwu.aitok.model.notice.domain.Notice;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.swing.plaf.PanelUI;

/**
 * NoticePageDTO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/16
 **/
@NoArgsConstructor
@Data
public class NoticePageDTO  {
    private Integer pageNum;
    private Integer pageSize;

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

}
