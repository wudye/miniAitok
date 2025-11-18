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
    private Notice notice;

    public NoticePageDTO(Notice notice) {
        this.notice = notice;
    }

    public static NoticePageDTO of(Notice notice) {
        return new NoticePageDTO(notice);
    }

}
