package com.mwu.aitolk.feign.social;


import com.mwu.aitiokcoomon.core.constant.ServiceNameConstants;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitolk.feign.social.fallback.RemoteSocialServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * RemoteSocialService
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/4
 **/
@FeignClient(contextId = "remoteSocialService", value = ServiceNameConstants.SOCIAL_SERVICE, fallbackFactory = RemoteSocialServiceFallback.class)
public interface RemoteSocialService {

    /**
     * 是否关注某人
     *
     * @param userId
     * @return
     */
    @GetMapping("/api/v1/follow/weatherfollow/{userId}")
    R<Boolean> weatherfollow(@PathVariable("userId") Long userId);

}
