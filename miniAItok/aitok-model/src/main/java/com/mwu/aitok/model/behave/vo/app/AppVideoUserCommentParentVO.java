package com.mwu.aitok.model.behave.vo.app;

import com.mwu.aitok.model.behave.domain.VideoUserComment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * app端视频父评论
 **/
@Data
@NoArgsConstructor
public class AppVideoUserCommentParentVO  {

    private VideoUserComment base;
    // 评论者昵称
    private String nickName;
    // 评论者头像
    private String avatar;
    // 子评论数量
    private Long childrenCount;

    public AppVideoUserCommentParentVO(VideoUserComment base) {
        this.base = base;
    }


    public static AppVideoUserCommentParentVO from(VideoUserComment base) {
        return new AppVideoUserCommentParentVO(base);
    }

}
