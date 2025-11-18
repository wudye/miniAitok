package com.mwu.aitok.model.behave.vo.app;

import com.mwu.aitok.model.behave.domain.UserFavorite;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 收藏夹vo
 *
 * @AUTHOR: mwu
 * @DATE: 2024/4/24
 **/
@Data
@NoArgsConstructor
public class FavoriteFolderVO  {

    private UserFavorite userFavorite;
    // 收藏视频数量
    private Long videoCount;
    // 该视频是否在此收藏夹中
    private Boolean weatherFavorite;

    public FavoriteFolderVO(UserFavorite userFavorite) {
        this.userFavorite = userFavorite;
    }
    public static FavoriteFolderVO from(UserFavorite userFavorite) {
        return new FavoriteFolderVO(userFavorite);
    }
}
