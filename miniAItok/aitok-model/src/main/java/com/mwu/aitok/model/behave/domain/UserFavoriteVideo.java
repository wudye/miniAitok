package com.mwu.aitok.model.behave.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * (UserFavoriteVideo)实体类
 *
 * @author mwu
 * @since 2023-11-17 10:16:03
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_favorite_video")
public class UserFavoriteVideo implements Serializable {
    private static final long serialVersionUID = -25561243481195565L;
    /**
     * 收藏夹id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long favoriteId;
    /**
     * 视频id
     */
    private String videoId;

    /**
     * 用户id
     */
    private Long userId;

    private LocalDateTime createTime;


}

