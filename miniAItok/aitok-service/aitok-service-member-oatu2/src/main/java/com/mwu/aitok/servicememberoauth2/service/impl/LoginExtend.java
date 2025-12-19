package com.mwu.aitok.servicememberoauth2.service.impl;

public class LoginExtend {
}

// java
// 放入 MemberServiceImpl 类（在类体内），并在 login 流程中按需调用

/*
private static final String LOGIN_FAIL_PREFIX = "login:fail:";
private static final String LOGIN_LOCK_PREFIX = "login:lock:";
private static final int FAIL_LIMIT = 3;
private static final long FAIL_WINDOW_SECONDS = 5 * 60; // 5 分钟窗口
private static final long LOCK_SECONDS = 30 * 60; // 锁定 30 分钟

private enum LockStrategy {
    USER_ID,
    IP_USERNAME,
    IP
}

private String buildFailKey(String username, Long userId, LockStrategy strategy) {
    switch (strategy) {
        case USER_ID:
            return LOGIN_FAIL_PREFIX + "user:" + (userId == null ? username : userId);
        case IP_USERNAME:
            String ip1 = IpUtils.getIpAddr(ServletUtils.getRequest());
            return LOGIN_FAIL_PREFIX + "ip_user:" + ip1 + ":" + username;
        case IP:
        default:
            String ip2 = IpUtils.getIpAddr(ServletUtils.getRequest());
            return LOGIN_FAIL_PREFIX + "ip:" + ip2;
    }
}

private String buildLockKey(String username, Long userId, LockStrategy strategy) {
    switch (strategy) {
        case USER_ID:
            return LOGIN_LOCK_PREFIX + "user:" + (userId == null ? username : userId);
        case IP_USERNAME:
            String ip1 = IpUtils.getIpAddr(ServletUtils.getRequest());
            return LOGIN_LOCK_PREFIX + "ip_user:" + ip1 + ":" + username;
        case IP:
        default:
            String ip2 = IpUtils.getIpAddr(ServletUtils.getRequest());
            return LOGIN_LOCK_PREFIX + "ip:" + ip2;
    }
}

private boolean isLocked(String username, Long userId, LockStrategy strategy) {
    String lockKey = buildLockKey(username, userId, strategy);
    Object locked = redisService.getCacheObject(lockKey);
    return locked != null;
}

/**
 * 记录一次失败尝试，返回 true 表示此操作导致账号/组合被锁定

private boolean recordFailedAttempt(String username, Long userId, LockStrategy strategy) {
    String failKey = buildFailKey(username, userId, strategy);
    String lockKey = buildLockKey(username, userId, strategy);

    Object obj = redisService.getCacheObject(failKey);
    int count = 0;
    try {
        if (obj != null) {
            count = Integer.parseInt(String.valueOf(obj));
        }
    } catch (Exception ignored) {
        count = 0;
    }
    count++;
    if (count >= FAIL_LIMIT) {
        // 设置锁并清除计数键
        redisService.setCacheObject(lockKey, "1");
        redisService.expire(lockKey, LOCK_SECONDS, TimeUnit.SECONDS);
        try { redisService.deleteObject(failKey); } catch (Exception ignored) {}
        return true;
    } else {
        // 更新失败计数并重置窗口 TTL
        redisService.setCacheObject(failKey, count);
        redisService.expire(failKey, FAIL_WINDOW_SECONDS, TimeUnit.SECONDS);
        return false;
    }
}

private void resetFailedAttempts(String username, Long userId, LockStrategy strategy) {
    String failKey = buildFailKey(username, userId, strategy);
    String lockKey = buildLockKey(username, userId, strategy);
    try { redisService.deleteObject(failKey); } catch (Exception ignored) {}
    try { redisService.deleteObject(lockKey); } catch (Exception ignored) {}
}
*/
/*
 使用建议（在 login 方法中）：
 1. 在能获取 username 之前，可按 IP 策略检查/记录（若希望按 IP 限制）。
 2. 在有 username（但未获取 userId）时，可按 IP+用户名 检查/记录，防止用户名枚举与暴力。
 3. 在查询到 Member 并拿到 userId 后，可再按 USER_ID 策略检查/记录（更精确的锁定）。

 示例调用（片段）：
 // 在 login 开始处（已有 username）：
 if (isLocked(username, null, LockStrategy.IP_USERNAME)) {
     throw new CustomException(HttpCodeEnum.TOO_MANY_REQUESTS);
 }
 // 查库后，如 memberOptional.isEmpty() 或 密码错误：
 boolean lockedNow = recordFailedAttempt(username, memberIdOrNull, LockStrategy.IP_USERNAME);
 if (lockedNow) throw new CustomException(HttpCodeEnum.TOO_MANY_REQUESTS);
 // 登录成功时：
 resetFailedAttempts(username, memberId, LockStrategy.IP_USERNAME);
 resetFailedAttempts(username, memberId, LockStrategy.USER_ID); // 如同时使用两种策略
*/
