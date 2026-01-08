package com.mwu.aitok.model.video.domain;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * 视频图片关联表(VideoImage)实体类
 *
 * @author mwu
 * @since 2023-11-20 21:18:59
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity

@Table(name = "video_image")
public class VideoImage implements Serializable {
    private static final long serialVersionUID = -25511217734667800L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 视频ID
     */
    private String videoId;
    /**
     * 图片地址
     */
    private String imageUrl;


}

