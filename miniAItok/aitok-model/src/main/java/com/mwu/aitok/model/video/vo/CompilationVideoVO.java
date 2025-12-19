package com.mwu.aitok.model.video.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * compilationVideoVO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/12/28
 **/
@Data
public class CompilationVideoVO {
    private String videoId;
    private String videoTitle;
    private String videoDesc;
    // 视频封面
    private String coverImage;
    // 视频地址
    private String videoUrl;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
