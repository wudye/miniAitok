package com.mwu.aitok.model.behave.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mwu.aitok.model.behave.domain.VideoNote;
import com.mwu.aitok.model.video.vo.Author;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 视频笔记表(VideoNote)实体类
 *
 * @author mwu
 * @since 2024-05-05 18:51:04
 */
@Data
@NoArgsConstructor
public class VideoNoteVO  {


   private Author author; // 作// 者

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

   private String delFlag;

   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
   private LocalDateTime createTime;

   private Long createBy;

   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
   private LocalDateTime updateTime;

   private Long updateBy;

   private String remark;
}

