// java
package com.mwu.aitokcommon.cache.ratelimiter.core.keyresolver.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitokcommon.cache.ratelimiter.core.annotation.RateLimiter;
import com.mwu.aitokcommon.cache.ratelimiter.core.keyresolver.RateLimiterKeyResolver;
import org.aspectj.lang.JoinPoint;

/**
 * 用户级别的限流 Key 解析器，使用方法名 + 方法参数 + userId + userType，组装成一个 Key
 * 为了避免 Key 过长，使用 MD5 进行“压缩”
 */
public class UserRateLimiterKeyResolver implements RateLimiterKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, RateLimiter rateLimiter) {
        String methodName = joinPoint.getSignature().toString();
        String argsStr = StringUtils.join(joinPoint.getArgs(), ",");
        Long userId = UserContext.getUserId();
        String userIdStr = userId == null ? "" : userId.toString();
        return DigestUtils.md5Hex(methodName + argsStr + userIdStr);
    }

}
