package com.mwu.aitok.model.behave.vo;


import com.mwu.aitok.model.behave.domain.VideoNote;
import com.mwu.aitok.model.video.vo.Author;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频笔记表(VideoNote)实体类
 *
 * @author mwu
 * @since 2024-05-05 18:51:04
 */
@Data
@NoArgsConstructor
public class VideoNoteVO  {

   private VideoNote videoNote;

   private Author author; // 作// 者


   public VideoNoteVO(VideoNote videoNote) {
      this.videoNote = videoNote;
   }

   public static VideoNoteVO from(VideoNote videoNote) {
      return new VideoNoteVO(videoNote);
   }
}

