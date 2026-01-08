// java
package com.mwu.aitokcommon.cache.ratelimiter.core.keyresolver.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import com.mwu.aitokcommon.cache.ratelimiter.core.annotation.RateLimiter;
import com.mwu.aitokcommon.cache.ratelimiter.core.keyresolver.RateLimiterKeyResolver;
import org.aspectj.lang.JoinPoint;

/**
 * 默认（全局级别）限流 Key 解析器，使用方法名 + 方法参数，组装成一个 Key
 *
 * 为了避免 Key 过长，使用 MD5 进行“压缩”
 */
public class DefaultRateLimiterKeyResolver implements RateLimiterKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, RateLimiter rateLimiter) {
        String methodName = joinPoint.getSignature().toString();
        String argsStr = StringUtils.join(joinPoint.getArgs(), ",");
        return DigestUtils.md5Hex(methodName + argsStr);
    }

}
