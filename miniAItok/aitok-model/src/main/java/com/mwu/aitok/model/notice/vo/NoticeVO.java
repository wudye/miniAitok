package com.mwu.aitok.model.notice.vo;

import com.mwu.aitok.model.notice.domain.Notice;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * NoticeVO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/16
 **/
@Data
public class NoticeVO  {
    private String nickName;
    private String operateAvatar;
    private String videoCoverImage;

    private Notice notice;

    public NoticeVO(Notice notice) {
        this.notice = notice;
    }

    public static NoticeVO of(Notice notice) {
        return new NoticeVO(notice);
    }
}
