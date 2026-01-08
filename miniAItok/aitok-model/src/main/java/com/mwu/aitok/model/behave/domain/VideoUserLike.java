package com.mwu.aitok.model.behave.domain;



import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点赞表(VideoUserLike)实体类
 *
 * @author mwu
 * @since 2023-10-30 14:32:59
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "video_user_like")
public class VideoUserLike implements Serializable {
    private static final long serialVersionUID = 366516787359335038L;
    /**
     * 点赞表id，记录总数即为点赞总数
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

