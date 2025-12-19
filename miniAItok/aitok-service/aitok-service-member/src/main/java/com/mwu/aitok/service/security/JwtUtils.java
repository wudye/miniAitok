package com.mwu.aitok.service.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Data
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600}")
    private Long jwtExpirationMs;



    /**
     * 生成JWT令牌
     */
    public String generateToken(String userName, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userName", userName);
        claims.put("role", role);


        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs * 1000))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 从token中获取用户
     */
    public String getUserAccountFromToken(String token) {

        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSignKey())
                    .build()

                    .parseSignedClaims(token)
                    .getPayload();

            return (String) claims.get("userName");
        } catch (Exception e) {
            return null;
        }
    }


    public String getUserRoleFromToken(String token) {

        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSignKey())
                    .build()

                    .parseSignedClaims(token)
                    .getPayload();

            return (String) claims.get("role");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证token是否有效
     * 这行代码的核心是使用 Jwts.parser() 创建一个 JWT 解析器，并通过 verifyWith 方法设置签名验证的密钥。getSignKey() 方法返回了一个基于 jwtSecret 的 SecretKey，用于验证 JWT 的签名是否正确。
     * build() 方法完成了解析器的配置，随后调用 parseSignedClaims(token) 方法解析传入的 token。如果令牌的签名无效、格式错误或已过期，解析过程会抛出异常
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) getSignKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新JWT令牌
     */
    public String refreshToken(String userName, String role) {
        return generateToken(userName, role);
    }





    // 生成 refreshToken（长有效期）
    public String generateRefreshToken(String userName,String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userName", userName);
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 例如 7天
                .signWith(getSignKey())
                .compact();
    }

    // 校验 refreshToken
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }


    private Key getSignKey() {

        byte[] bytes = Base64.getDecoder()
                .decode(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(bytes, "HmacSHA256");
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    // no expired return false
    public boolean isExpired(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration().before(new Date());
    }
    public boolean isLogin(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);



        return validateToken(token) && !isExpired(token);

    }

}
