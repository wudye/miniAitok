package com.mwu.aitok.model.search.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PageDTO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/31
 **/
@Data
@NoArgsConstructor
public class PageDTO {
    private Integer pageNum;
    private Integer pageSize;
}
