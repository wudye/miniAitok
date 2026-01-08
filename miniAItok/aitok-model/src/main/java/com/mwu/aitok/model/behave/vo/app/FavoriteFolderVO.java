package com.mwu.aitok.model.behave.vo.app;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mwu.aitok.model.behave.domain.UserFavorite;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

/**
 * 收藏夹vo
 *
 * @AUTHOR: mwu
 * @DATE: 2024/4/24
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteFolderVO  {


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
    // 该视频是否在此收藏夹中
    private Boolean weatherFavorite;


}
