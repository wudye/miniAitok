package com.mwu.aitok.model.social.vo;

import lombok.Data;

/**
 * Fans
 *
 * @AUTHOR: mwu
 * @DATE: 2024/4/21
 * 粉丝分页vo
 **/
@Data
public class Fans {
    private Long userId;
    private String avatar;
    private String nickName;
    private String sex;
    private Boolean weatherFollow; // 是否关注
}
