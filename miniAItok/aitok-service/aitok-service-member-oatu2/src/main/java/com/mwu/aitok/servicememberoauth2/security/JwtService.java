// java
// File: src/main/java/com/mwu/aitok/servicememberoauth2/security/JwtService.java
package com.mwu.aitok.servicememberoauth2.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitok.servicememberoauth2.entity.TokenEntity;
import com.mwu.aitok.servicememberoauth2.entity.TokenPair;
import com.mwu.aitok.servicememberoauth2.repository.TokenRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private static final String ISSUER = "aitok-member";
    private final RSAKey rsaJWK;
    private final TokenRepository tokenRepository;

    public JwtService(TokenRepository tokenRepository) throws Exception {
        this.tokenRepository = tokenRepository;

        Path dir = Path.of(System.getProperty("user.home"), ".aitok");
        Files.createDirectories(dir);
        Path keyFile = dir.resolve("jwt-rsa-jwk.json");

        if (Files.exists(keyFile)) {
            String json = Files.readString(keyFile);
            rsaJWK = RSAKey.parse(json);
        } else {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();
            rsaJWK = new RSAKey.Builder((java.security.interfaces.RSAPublicKey) kp.getPublic())
                    .privateKey((RSAPrivateKey) kp.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();
            Path tmp = dir.resolve("jwt-rsa-jwk.json.tmp");
            Files.writeString(tmp, rsaJWK.toJSONString());
            Files.move(tmp, keyFile);
        }
    }

    // 现有的通用方法，生成 JWT（返回值写法避免直接重复片段）
    public String createToken(Map<String, Object> customClaims) throws Exception {
        JWSSigner signer = new RSASSASigner(rsaJWK.toPrivateKey());
        Instant now = Instant.now();

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(3600)));

        Object username = customClaims.get("username");
        if (username != null) {
            claimsBuilder.subject(username.toString());
        }

        for (Map.Entry<String, Object> e : customClaims.entrySet()) {
            claimsBuilder.claim(e.getKey(), e.getValue());
        }

        JWTClaimsSet claims = claimsBuilder.build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaJWK.getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claims);
        signedJWT.sign(signer);
        String token = signedJWT.serialize();
        return token;
    }

    // 生成 refresh token（不使用 JWT），并同时保存到 DB + JSON 文件
    @Transactional
    public TokenPair createAndStoreTokens(String username, String userId) throws Exception {
        // 1. 创建 access token
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("userid", userId);
        // 如需 roles 等可加入 claims.put("roles", List.of(...));
        String accessToken = createToken(claims);

        // 2. 创建 refresh token（示例为随机 UUID；生产请存储其哈希）
        String refreshToken = UUID.randomUUID().toString();

        Instant now = Instant.now();
        Instant accessExpiry = now.plusSeconds(3600);
        Instant refreshExpiry = now.plusSeconds(7 * 24 * 3600); // 7 天

        // 3. 保存到数据库
        TokenEntity entity = new TokenEntity(userId, accessToken, refreshToken, accessExpiry, refreshExpiry, now);
        tokenRepository.save(entity);

        // 4. 写入用户目录下的 JSON 文件，供本地或测试使用
        Path dir = Path.of(System.getProperty("user.home"), ".aitok", "tokens");
        Files.createDirectories(dir);
        Path out = dir.resolve(userId + ".json");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> outMap = new HashMap<>();
        outMap.put("accessToken", accessToken);
        outMap.put("refreshToken", refreshToken);
        outMap.put("accessExpiry", accessExpiry.toString());
        outMap.put("refreshExpiry", refreshExpiry.toString());
        outMap.put("createdAt", now.toString());
        mapper.writeValue(out.toFile(), outMap);

        return new TokenPair(accessToken, refreshToken, accessExpiry, refreshExpiry);
    }

    // 可选：按 refresh token 查找并续期的示例（省略具体实现）
    public TokenEntity findByRefreshToken(String refreshToken) {
        return tokenRepository.findByRefreshToken(refreshToken).orElse(null);
    }

    public JWKSet getJwkSet() {
        return new JWKSet(rsaJWK.toPublicJWK());
    }
}
