package com.mwu.aitok.model.ai.dto.model;

import com.mwu.aitok.model.search.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ToolPageDTO
 *
 * @AUTHOR: mwu
 * @DATE: 2025/6/5
 **/
@Data
public class ToolPageDTO extends PageDTO {

    @Schema(description = "工具名称", example = "王五")
    private String name;
    private String description;

    @Schema(description = "状态", example = "1")
    private String stateFlag;
}
