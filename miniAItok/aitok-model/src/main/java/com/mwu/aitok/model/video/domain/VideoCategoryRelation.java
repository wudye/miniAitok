package com.mwu.aitok.model.video.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * 视频分类关联表(VideoCategoryRelation)实体类
 *
 * @author mwu
 * @since 2023-10-31 14:44:34
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "video_category_relation")
public class VideoCategoryRelation implements Serializable {
    private static final long serialVersionUID = -53464330298328844L;

    /**
     * 视频id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String videoId;
    /**
     * 分类id
     */
    private Long categoryId;


}

