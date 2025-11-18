package com.mwu.aitok.model.video.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 敏感词信息表(VideoSensitive)实体类
 *
 * @author lzq
 * @since 2023-10-30 11:17:39
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "video_sensitive")
public class VideoSensitive implements Serializable {

    private static final long serialVersionUID = 887830254999098288L;
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 敏感词
     */
    private String sensitives;
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;


}

