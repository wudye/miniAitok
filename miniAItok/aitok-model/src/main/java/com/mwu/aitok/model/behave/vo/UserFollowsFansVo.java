package com.mwu.aitok.model.behave.vo;

import lombok.Data;

/**
 * 功能：
 * 作者：mwu
 * 日期：2023/11/9 20:08
 */
@Data
public class UserFollowsFansVo {

    /*
     *关注数
     */
    private Long followedNums;

    /*
     *粉丝数
     */
    private Long fanNums;

}
