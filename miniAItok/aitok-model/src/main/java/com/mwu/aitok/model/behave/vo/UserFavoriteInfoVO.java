package com.mwu.aitok.model.behave.vo;

import com.mwu.aitok.model.behave.domain.UserFavorite;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * UserFavoriteInfoVO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/19
 **/
@NoArgsConstructor
@Data
public class UserFavoriteInfoVO {

    private UserFavorite userFavorite;
    // 收藏视频数量
    private Long videoCount;

    // 最近收藏视频封面集合
    private String[] videoCoverList;


    public UserFavoriteInfoVO(UserFavorite userFavorite) {
        this.userFavorite = userFavorite;
    }

    public static UserFavoriteInfoVO from(UserFavorite userFavorite) {
        return new UserFavoriteInfoVO(userFavorite);
    }
}
