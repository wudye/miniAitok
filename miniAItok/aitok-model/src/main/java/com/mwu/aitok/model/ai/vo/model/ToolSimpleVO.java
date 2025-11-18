package com.mwu.aitok.model.ai.vo.model;

import lombok.Data;

/**
 * ToolSimpleVO
 *
 * @AUTHOR: mwu
 * @DATE: 2025/6/5
 **/
@Data
public class ToolSimpleVO {
    /**
     * 工具编号
     */

    private Long id;
    /**
     * 工具名称
     */
    private String name;
    /**
     * 工具描述
     */
    private String description;
}
