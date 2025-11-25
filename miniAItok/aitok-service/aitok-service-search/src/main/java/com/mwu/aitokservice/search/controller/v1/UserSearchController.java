package com.mwu.aitokservice.search.controller.v1;

import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.search.dto.UserSearchKeywordDTO;
import com.mwu.aitokservice.search.domain.vo.UserSearchVO;
import com.mwu.aitokservice.search.service.UserSearchService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户搜索控制层
 *
 * @AUTHOR: roydon
 * @DATE: 2024/10/11
 **/
@RestController
@RequestMapping("/api/v1/user")
public class UserSearchController {

    @Resource
    private UserSearchService userSearchService;

    /**
     * 分页搜索用户
     */
    @PostMapping()
    public R<List<UserSearchVO>> searchVideo(@Validated @RequestBody UserSearchKeywordDTO dto) {
        List<UserSearchVO> userSearchResultList = userSearchService.searchUserFromES(dto);
        return R.ok(userSearchResultList);
    }
}
