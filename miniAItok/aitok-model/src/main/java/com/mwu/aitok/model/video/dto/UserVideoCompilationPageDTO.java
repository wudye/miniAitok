package com.mwu.aitok.model.video.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserVideoCompilationPageDTO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/27
 **/
@NoArgsConstructor

@Data
public class UserVideoCompilationPageDTO  {
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    private Long compilationId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 合集标题
     */

    private String title;
    /**
     * 描述
     */
    private String description;
    /**
     * 合集封面(5M)
     */
    private String coverImage;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;



}
