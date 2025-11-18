// java
package com.mwu.aitokcommon.cache.ratelimiter.core.keyresolver.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import com.mwu.aitokcommon.cache.ratelimiter.core.annotation.RateLimiter;
import com.mwu.aitokcommon.cache.ratelimiter.core.keyresolver.RateLimiterKeyResolver;
import org.aspectj.lang.JoinPoint;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Server 节点级别的限流 Key 解析器，使用方法名 + 方法参数 + 节点信息组装 Key，并用 MD5 压缩
 */
public class ServerNodeRateLimiterKeyResolver implements RateLimiterKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, RateLimiter rateLimiter) {
        String methodName = joinPoint.getSignature().toString();
        String argsStr = StringUtils.join(joinPoint.getArgs(), ",");
        String hostAddress = "unknown";
        try {
            InetAddress local = InetAddress.getLocalHost();
            if (local != null && local.getHostAddress() != null) {
                hostAddress = local.getHostAddress();
            }
        } catch (UnknownHostException ignored) {
        }
        long pid = ProcessHandle.current().pid();
        String serverNode = String.format("%s@%d", hostAddress, pid);
        return DigestUtils.md5Hex(methodName + argsStr + serverNode);
    }

}
