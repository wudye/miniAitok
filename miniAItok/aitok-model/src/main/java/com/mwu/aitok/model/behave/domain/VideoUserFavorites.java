package com.mwu.aitok.model.behave.domain;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频收藏表(VideoUserFavorites)实体类
 * 此表表示用户仅仅收藏视频，与收藏夹无关
 *
 * @author lzq
 * @since 2023-10-31 15:57:38
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "video_user_favorites")
public class VideoUserFavorites implements Serializable {
    private static final long serialVersionUID = -50448230889868246L;
    /**
     * 收藏表id，总数即为s
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /**
     * 视频id
     */
    private String videoId;


    /**
     * 用户id
     */
    private Long userId;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}

