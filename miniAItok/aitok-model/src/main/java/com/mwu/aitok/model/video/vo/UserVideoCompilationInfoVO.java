package com.mwu.aitok.model.video.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.swing.plaf.PanelUI;
import java.time.LocalDateTime;

/**
 * web页面
 * 大屏播放视频合集tab
 *
 * @AUTHOR: mwu
 * @DATE: 2024/5/15
 **/
@Data
@NoArgsConstructor
public class UserVideoCompilationInfoVO  {
    private Long playCount;
    private Long favoriteCount;
    private Long videoCount;
    private Boolean weatherFollow;

    private Long compilationId;

    private Long userId;

    private String title;

    private String description;

    private String coverImage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
