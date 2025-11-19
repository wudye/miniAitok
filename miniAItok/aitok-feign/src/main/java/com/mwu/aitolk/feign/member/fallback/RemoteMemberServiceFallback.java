package com.mwu.aitolk.feign.member.fallback;

import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitolk.feign.member.RemoteMemberService;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class RemoteMemberServiceFallback implements FallbackFactory<RemoteMemberService> {
    @Override
    public RemoteMemberService create(Throwable cause) {
        return new RemoteMemberService() {
            @Override
            public R<Member> userInfoById(Long userId) {
//                return R.fail("获取信息失败:" + cause.getMessage());
                return R.fail(new Member());
            }
        };
    }
}