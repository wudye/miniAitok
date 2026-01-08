package com.mwu.aitokservice.search.service;



import com.mwu.aitok.model.search.dto.UserSearchKeywordDTO;
import com.mwu.aitokservice.search.domain.vo.UserSearchVO;

import java.util.List;

/**
 * UserSearchService
 *
 * @AUTHOR: mwu
 * @DATE: 2024/10/11
 **/
public interface UserSearchService {
    /**
     * 从es分页搜索用户
     */
    List<UserSearchVO> searchUserFromES(UserSearchKeywordDTO dto);
}
