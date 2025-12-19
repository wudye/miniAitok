package com.mwu.aitok.model.creator.dto;

import lombok.Data;

/**
 * 视频合集分页dto
 *
 * @AUTHOR: mwu
 * @DATE: 2023/12/5
 **/
@Data
public class videoCompilationPageDTO {
    private Long userId;
    private String title;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
