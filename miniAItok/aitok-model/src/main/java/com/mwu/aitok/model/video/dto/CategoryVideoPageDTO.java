package com.mwu.aitok.model.video.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类视频分页dto
 *
 * @AUTHOR: mwu
 * @DATE: 2024/2/5
 **/
@Data
public class CategoryVideoPageDTO {
    @NotNull
    private Long id; // 分类id
    @NotNull
    private Integer pageNum; // 页码
    @NotNull
    private Integer pageSize; // 每页数量
}
