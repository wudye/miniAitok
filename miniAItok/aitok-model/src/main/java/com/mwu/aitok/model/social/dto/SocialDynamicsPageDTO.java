package com.mwu.aitok.model.social.dto;

import com.mwu.aitok.model.search.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社交动态分页dto
 *
 * @AUTHOR: mwu
 * @DATE: 2024/4/7
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class SocialDynamicsPageDTO extends PageDTO {
    private Long userId;
}
