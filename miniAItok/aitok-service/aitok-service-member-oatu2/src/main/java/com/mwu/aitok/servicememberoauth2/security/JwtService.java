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
import java.util.*;

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
        //跨平台兼容性:
        //Windows: C:\Users\<username>\.aitok\jwt-rsa-jwk.json
        //Linux/Mac: /home/<username>/.aitok/jwt-rsa-jwk.json

        if (Files.exists(keyFile)) {
            String json = Files.readString(keyFile);
            // 使用 Nimbus JOSE + JWT 库解析 JSON 为 RSAKey 对象
            rsaJWK = RSAKey.parse(json);
        } else {
            /*
            使用 Java 标准库创建 RSA 密钥对生成器
            设置密钥长度为 2048 位（当前安全标准）
            生成公钥和私钥对
            构造 JWK 格式的 RSAKey 对象
            为密钥分配唯一标识符（UUID）
             */
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
        JWSSigner signer = new RSASSASigner(rsaJWK.toPrivateKey()); // 使用已存在的私钥
        Instant now = Instant.now();

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject("system")
                .audience("app")
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

        //获取公钥 RSAKey publicRSAKey = (RSAKey) jwkSet.getKeyByKeyId(keyId);
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(rsaJWK.getKeyID()) //  使用密钥ID标识签名者
                .type(JOSEObjectType.JWT)
                .build();

        // / 使用私钥签名 JWT
        SignedJWT signedJWT = new SignedJWT(header, claims);
        signedJWT.sign(signer);
        String token = signedJWT.serialize();
        /*
        / RSAKey 序列化后的 JWK 格式示例
            {
              "kty": "RSA",
              "use": "sig",
              "kid": "abc123def456",
              "n": "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx4...",
              "e": "AQAB",
              "d": "X4cTteJY_gn4FYPsXB8rdXix5vwsg1FLN5E3EaG6RJoVH-HLLKD9..."
            }
         */
        return token;
    }

    /*
    jwt-rsa-jwk.json 是保存服务端 RSA 密钥对的文件，按代码会写入运行该后端进程的操作系统用户的家目录下的 /.aitok 目录（例如 C:\Users\<svcuser>\.aitok\jwt-rsa-jwk.json 或 Linux 的 /home/<svcuser>/.aitok/jwt-rsa-jwk.json）。
这是“每个后端实例/主机”一份文件，而不是为每个应用用户单独生成。换言之，运行该服务的同一个服务器（或容器）上所有登录用户都会使用同一把密钥签发/验证 access token。
对外验证只需公钥：服务通过 getJwkSet() 暴露公钥集合，外部或前端用该公钥验证 JWT 签名；私钥留在该文件中，必须严格保护，不能公开。
部署注意事项：
如果有多台后端实例，需保证它们共享同一密钥（通过共享文件、配置管理或使用 KMS/密钥库），否则一个实例签的 token 另一个实例无法验证。
在生产环境建议使用专门的密钥管理（KMS、Vault）或环境/容器秘密管理，避免把私钥明文写在主机文件系统上。
密钥轮换要显式实现：替换该文件后需协调令牌有效性或重新签发。
     */
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
        Instant refreshExpiry = now.plusSeconds(30 * 24 * 3600); // 7 天

        // 保存到数据库
        TokenEntity entity = new TokenEntity();
        entity.setUserId(userId);
        entity.setUsername(username);
        entity.setRefreshToken(refreshToken);
        entity.setRefreshExpiry(refreshExpiry);

        // 直接保存新记录，允许同一个 user_id 有多条记录
        tokenRepository.save(entity);


        // 4. 写入用户目录下的 JSON 文件，供本地或测试使用
        // TODO: 生产环境请delete此段，避免在文件系统存储敏感令牌
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
