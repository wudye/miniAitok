package com.mwu.aitok.model.behave.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 视频笔记表(VideoNote)实体类
 *
 * @author mwu
 * @since 2024-05-05 18:51:04
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "video_note")
public abstract class VideoNote implements Serializable {
    private static final long serialVersionUID = 136140011238956718L;
    /**
     * 笔记id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Long noteId;
    /**
     * 视频id
     */
    @NotBlank
    private String videoId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 笔记title
     */
    @Size(min = 1, max = 100)
    private String noteTitle;
    /**
     * 笔记内容
     */
    @Size(min = 1)
    private String noteContent;
    /**
     * 状态：0默认1禁止2删除
     *
     * @see com.aitok.model.behave.enums.VideoNoteDelFlagEnum
     */
    private String delFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    private Long createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    private Long updateBy;

    private String remark;


}

