package com.mwu.aitolk.feign.member;

import com.mwu.aitiokcoomon.core.constant.ServiceNameConstants;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitolk.feign.member.fallback.RemoteMemberServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(contextId = "remoteMemberService", value = ServiceNameConstants.USER_SERVICE,
       fallbackFactory = RemoteMemberServiceFallback.class)
public interface RemoteMemberService {



        /**
         * 获取用户信息
         */
        @GetMapping("/api/v1/{userId}")
        R<Member> userInfoById(@PathVariable Long userId);
}
