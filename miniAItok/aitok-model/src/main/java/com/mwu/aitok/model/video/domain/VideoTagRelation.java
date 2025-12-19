package com.mwu.aitok.model.video.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * 视频标签关联表(VideoTagRelation)实体类
 *
 * @author mwu
 * @since 2023-11-11 17:19:09
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "video_tag_relation")
public class VideoTagRelation implements Serializable {
    private static final long serialVersionUID = -77325744334344037L;
    /**
     * 视频id
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String videoId;
    /**
     * 标签id
     */
    private Long tagId;

}

