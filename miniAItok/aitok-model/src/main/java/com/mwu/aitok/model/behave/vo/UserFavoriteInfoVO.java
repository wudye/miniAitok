package com.mwu.aitok.model.behave.vo;

import lombok.*;

import java.time.LocalDateTime;

/**
 * UserFavoriteInfoVO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/19
 **/
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class UserFavoriteInfoVO {

    private Long favoriteId;
    /**
     * 用户id
     */
    private Long userId;

    private String title;

    private String description;
    /**
     * 收藏夹封面
     */
    private String coverImage;

    private LocalDateTime createTime;
    /**
     * 0:别人可见，1:陌生人不可见
     */
    private String showStatus;
    /**
     * 0存在，1删除
     */
    private String delFlag;
    // 收藏视频数量
    private Long videoCount;

    // 最近收藏视频封面集合
    private String[] videoCoverList;


}
