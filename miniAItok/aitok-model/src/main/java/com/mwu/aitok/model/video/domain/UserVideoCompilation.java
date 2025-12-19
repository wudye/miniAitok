package com.mwu.aitok.model.video.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视频合集表(UserVideoCompilation)实体类
 *
 * @author mwu
 * @since 2023-11-27 18:08:39
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_video_compilation")
public class UserVideoCompilation implements Serializable {
    private static final long serialVersionUID = -19314287338282438L;
    /**
     * compilation_id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compilation_id")
    private Long compilationId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 合集标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 20, message = "标题需在20字符以内")
    private String title;
    /**
     * 描述
     */
    @Size(min = 1, max = 200, message = "描述需在200字符以内")
    private String description;
    /**
     * 合集封面(5M)
     */
    private String coverImage;
    /**
     * 创建时间
     */
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;


}

