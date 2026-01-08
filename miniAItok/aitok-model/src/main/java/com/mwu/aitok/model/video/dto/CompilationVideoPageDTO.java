package com.mwu.aitok.model.video.dto;

import lombok.Data;

/**
 * CompilationVideoPageDTO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/12/28
 **/
@Data
public class CompilationVideoPageDTO {

    private Long compilationId;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
