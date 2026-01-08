package com.mwu.aitok.servicememberoauth2.security;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
Spring Security 内置验证流程
Spring Security 在接收到带有 Authorization: Bearer <JWT> 的请求时，会自动执行以下验证步骤：

令牌提取

从请求头中提取 JWT 令牌
解析令牌格式
签名验证

从 JWKS 端点获取公钥
使用公钥验证令牌签名
声明验证

检查过期时间(exp)
验证生效时间(nbf)
检查发行者(iss)
验证受众(aud)
构建认证对象

创建 JwtAuthenticationToken
设置认证状态为已认证
Spring Security 的这两行配置确实会自动实现 JWT 令牌的验证功能，相当于提供了一个内置的 JwtValidator

 */
public class JwtValidator {

    private final JwtService jwtService;

    public JwtValidator(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public boolean validateToken(String token) {
        try {
            // 1. 解析JWT
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 2. 获取公钥
            String keyId = signedJWT.getHeader().getKeyID();
            JWKSet jwkSet = jwtService.getJwkSet();
            RSAKey rsaKey = (RSAKey) jwkSet.getKeyByKeyId(keyId);

            // 3. 验证签名
            RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);
            if (!signedJWT.verify(verifier)) {
                return false;
            }

            // 4. 验证声明
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            // 检查是否过期
            if (claimsSet.getExpirationTime().before(new Date())) {
                return false;
            }

            // 检查发行者
            if (!"aitok-member".equals(claimsSet.getIssuer())) {
                return false;
            }

            // 所有验证通过
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> extractClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            Map<String, Object> claims = new HashMap<>();
            claims.put("issuer", claimsSet.getIssuer());
            claims.put("subject", claimsSet.getSubject());
            claims.put("username", claimsSet.getClaim("username"));
            claims.put("userId", claimsSet.getClaim("userid"));
            claims.put("expirationTime", claimsSet.getExpirationTime());

            return claims;

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
