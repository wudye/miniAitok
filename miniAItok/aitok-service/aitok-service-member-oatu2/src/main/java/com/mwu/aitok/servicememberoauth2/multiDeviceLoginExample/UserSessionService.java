package com.mwu.aitok.servicememberoauth2.multiDeviceLoginExample;


import com.mwu.aitok.servicememberoauth2.entity.TokenPair;
import com.mwu.aitok.servicememberoauth2.security.JwtService;
import com.mwu.aitok.servicememberoauth2.constants.UserCacheConstants;
import com.mwu.aitokcommon.cache.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final RedisService redisService;
    private final JwtService jwtService;

    private static final String SESSION_PREFIX = "user:session:";      // user:session:{userId}:{deviceId}
    private static final String JTI_PREFIX = "token:jti:";            // token:jti:{jti} -> sessionKey
    private static final String SESSIONS_INDEX = "user:sessions:";    // user:sessions:{userId} -> List<String> deviceIds

    /*
    public void createSession(Long userId, TokenPair tokenPair, DeviceInfo deviceInfo) {
        String deviceId = deviceInfo.getDeviceId();
        String sessionKey = SESSION_PREFIX + userId + ":" + deviceId;

        // 存 session（TokenPair），过期按你系统策略（这里用 SESSION_EXPIRE_SECONDS）
        redisService.setCacheObject(sessionKey, tokenPair);
        redisService.expire(sessionKey, UserCacheConstants.SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 存 jti -> sessionKey 映射，便于根据 token 快速找到 session
        String jti = jwtService.extractJti(tokenPair.accessToken());
        if (jti != null) {
            String jtiKey = JTI_PREFIX + jti;
            redisService.setCacheObject(jtiKey, sessionKey);
            redisService.expire(jtiKey, UserCacheConstants.SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }

        // 维护用户设备列表（简单实现：从 redis 读写 List\<String\>）
        String idxKey = SESSIONS_INDEX + userId;
        List<String> deviceList = redisService.getCacheObject(idxKey);
        if (deviceList == null) deviceList = new ArrayList<>();
        if (!deviceList.contains(deviceId)) {
            deviceList.add(deviceId);
            redisService.setCacheObject(idxKey, deviceList);
            redisService.expire(idxKey, UserCacheConstants.SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
    }

    public boolean isAccessTokenValid(String accessToken) {
        String jti = jwtService.extractJti(accessToken);
        if (jti == null) return false;
        String jtiKey = JTI_PREFIX + jti;
        String sessionKey = redisService.getCacheObject(jtiKey);
        if (sessionKey == null) return false;
        TokenPair session = redisService.getCacheObject(sessionKey);
        return session != null && accessToken.equals(session.accessToken());
    }

    public void logoutByDevice(Long userId, String deviceId) {
        String sessionKey = SESSION_PREFIX + userId + ":" + deviceId;
        TokenPair session = redisService.getCacheObject(sessionKey);
        if (session != null) {
            String jti = jwtService.extractJti(session.accessToken());
            if (jti != null) {
                redisService.deleteObject(JTI_PREFIX + jti);
            }
        }
        redisService.deleteObject(sessionKey);

        // 从设备列表中移除
        String idxKey = SESSIONS_INDEX + userId;
        List<String> deviceList = redisService.getCacheObject(idxKey);
        if (deviceList != null && deviceList.remove(deviceId)) {
            redisService.setCacheObject(idxKey, deviceList);
        }
    }

     */
}
