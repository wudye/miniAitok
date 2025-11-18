package com.mwu.aitok.model.video.dto;

import lombok.Data;

/**
 * VideoCategoryPageDTO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/6
 **/
@Data
public class VideoCategoryPageDTO {
    private Long categoryId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
