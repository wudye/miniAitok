package com.mwu.aitok.model.video.vo;

import lombok.Data;

/**
 * 视频作者
 *
 * @AUTHOR: mwu
 * @DATE: 2023/12/20
 **/
@Data
public class Author {
    private Long userId;
    private String userName;
    private String nickName;
    private String avatar;
}
