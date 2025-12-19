package com.mwu.aitok.model.video.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;

/**
 * 视频标签表(VideoTag)实体类
 *
 * @author mwu
 * @since 2023-11-11 16:05:08
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "video_tag")
public class VideoTag implements Serializable {
    private static final long serialVersionUID = -61414077398067907L;
    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;
    /**
     * 标签内容
     */
    @NotBlank(message = "名称不能为空")
    @Size(min = 1, max = 20, message = "标签不能超过20个字符")
    private String tag;

}

