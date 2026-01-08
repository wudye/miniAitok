package com.mwu.aitok.model.behave.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视频行为表(UserVideoBehave)实体类
 *
 * @author mwu
 * @since 2024-04-19 14:21:12
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_video_behave")
public class UserVideoBehave implements Serializable {
    private static final long serialVersionUID = 457075971707604771L;
    /**
     * id
     */
    @Column(name = "behave_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long behaveId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户行为0无行为1观看2点赞3评论4收藏
     */
    private String userBehave;
    /**
     * 视频ID
     */
    private String videoId;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @CreationTimestamp
    private LocalDateTime createTime;


}

