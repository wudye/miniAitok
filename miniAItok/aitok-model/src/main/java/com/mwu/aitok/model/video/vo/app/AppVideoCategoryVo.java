package com.mwu.aitok.model.video.vo.app;

import lombok.Data;

/**
 * AppVideoCategoryVo
 *
 * @AUTHOR: mwu
 * @DATE: 2024/2/5
 **/
@Data
public class AppVideoCategoryVo {
    /**
     * 分类id
     */
    private Long id;
    /**
     * 分类name
     */
    private String name;

    private String categoryImage;
}
