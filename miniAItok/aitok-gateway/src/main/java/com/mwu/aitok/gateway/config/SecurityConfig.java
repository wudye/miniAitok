package com.mwu.aitok.gateway.config;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Resource
    private JwtHeaderFilter jwtHeaderFilter;

    private static final String[] COMMON_PUBLIC = new String[]{
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/member/**"
    };


    @Bean
    org.springframework.web.cors.reactive.CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.addAllowedOriginPattern("*");
//        config.addAllowedMethod("*");
//        config.addAllowedHeader("*");
//        config.setAllowCredentials(true);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }



    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {

        /*
        Spring Security 的资源服务器过滤器尝试从请求中提取 Bearer token（通常从 Authorization: Bearer <token>）。
如果没有找到 token，过滤器不会创建 Authentication，SecurityContext 保持为空（即请求为匿名）。
随后执行授权规则：
         */

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults()) // 使用默认 CORS 配置或按需自定义
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(COMMON_PUBLIC).permitAll()
                        .anyExchange().authenticated()
                )
                /*
                Authentication 是服务器内存中的对象，保存在当前请求的
                 SecurityContext（在 Reactor 中是 Reactor Context）里，不会自动通过 HTTP 网络传给下游服务。
                 下游服务只能收到 HTTP 请求头/体，所以要么转发原始 Authorization: Bearer <token>，要么把需要的 claim 提取成 header（例如 X-User-Id）并发送。

                 网关的资源服务器过滤器先从请求提取 Authorization: Bearer <token>，然后调用容器中的 ReactiveJwtDecoder（你在 reactiveJwtDecoder() 中注册的 NimbusReactiveJwtDecoder）对 token 进行解码并执行校验（你已通过 JwtTimestampValidator(Duration.ofSeconds(0)) 做了严格的过期校验）。这是一个反应式步骤，解码/校验返回一个 Mono<Jwt>。
如果解码/校验失败（例如 token 已过期），过滤器会终止流程，不会创建 Authentication，最终会触发 401 响应；你的 jwtAuthenticationConverter 不会被调用。
如果解码/校验成功，则会得到 Jwt，接着执行你注册的 jwtAuthenticationConverter（你用 ReactiveJwtAuthenticationConverterAdapter(new CustomJwtAuthenticationConverter()) 包装的转换器）将 Jwt 转成 Authentication 并放入 SecurityContext，后续授权基于该 Authentication。
                 */
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                    jwt.jwtAuthenticationConverter(
                            new ReactiveJwtAuthenticationConverterAdapter(
                                    new CustomJwtAuthenticationConverter()
                            )
                    );
                }))
                /*
                如果你希望下游自己校验 token：保留 Authorization，并确保下游有相应的 OAuth2/JWT 配置（受众、scope、算法等一致）。
如果你只想在网关完成认证并以受信任的 header 转发用户信息：网关应验证后删除 Authorization，只转发安全的业务 header（例如 X-User-Id），并配合内网信任或 mTLS/签名防止伪造。
风险：转发原始 token 会扩大泄露面；删除 token 但不保证 header 不被伪造会有信任问题。
                 */
                // 启用 OAuth2 resource server 的 JWT 支持（会使用项目中声明的 ReactiveJwtDecoder Bean，如果有的话）
                // .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
                // 在认证后写入 USER_ID_HEADER 到请求头，位置选在 AUTHENTICATION 之后
                .addFilterAfter(jwtHeaderFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }


    @Value("${jwt.secret}")
    private String jwtSecret;




    // check the request jwttoken valid and expire or not
    /*
    因为 NimbusReactiveJwtDecoder 基于成熟的 Nimbus JOSE+JWT 库并与 Spring Security 原生集成，能提供正确、安全、可配置的解码与签名校验，避免自己重复实现容易出错的加解密/校验逻辑。
主要理由（要点）：
安全与正确性：Nimbus 实现了规范的 JWT/JWS/JWE 处理（签名算法、签名验证、时间声明校验等），经过社区广泛验证，比自写解析/校验更可靠。
与 Spring Security 集成：能直接作为 ReactiveJwtDecoder 使用，支持 OAuth2TokenValidator 链、标准异常类型和 Spring 的错误映射（自动导致 401 等），配置简单。
JWK/JWKS 支持：内置对 JWK Set URI、密钥轮换（key rotation）等的支持，便于集中式认证服务集成。
正确的算法/密钥处理：自动处理算法（alg）和密钥类型（对称/非对称），减少因实现错误导致的安全漏洞。
可扩展的校验链：可以通过 DelegatingOAuth2TokenValidator 等添加额外校验（如自定义业务校验），而不是把所有校验混在解码器实现中。
性能与反应式兼容：NimbusReactiveJwtDecoder 是为反应式栈设计的，避免不必要的线程切换或阻塞（而自有同步工具可能需要切换到 boundedElastic()）。
可维护性与可审计：使用标准库更容易被团队审计、升级和社区支持，减少自实现带来的长期维护成本。
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();

        // 严格时间校验（无宽限）以匹配 account 模块的行为
        /*
        JwtTimestampValidator 是一个内置的时间戳校验器，用于验证
        JWT 的时间声明（如 exp 和 nbf）。通过 Duration.ofSeconds(0) 配置，表示没有宽限时间，校验将严格按照令牌的时间戳进行验证。例如，如果令牌的过期时间（exp）已到，校验会立即失败
         */
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator(Duration.ofSeconds(0));
        /*
        ，DelegatingOAuth2TokenValidator 是一个组合校验器，它允许将多个校验器组合在一起。
        里将 timestampValidator 设置为 jwtDecoder 的校验器，确保在解码 JWT 时会执行时间戳校验。
         */
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestampValidator));

        return jwtDecoder;
    }
}
/*
TODO: frontend sends refresh token when access token expired, and backend refresh token and send new access token to frontend
当 JWT 过期时的行为（简要）：
校验阶段会失败：你的解码器里启用了严格的时间戳校验（没有宽限），因此对 exp / nbf 的检查会判定为无效，解码/校验会返回错误（在反应式流中会产生一个失败的 Mono，抛出与 JWT 校验相关的异常）。
不会产生 Authentication：因为解码/校验失败，Spring Security 不会为该请求创建有效的 Authentication，SecurityContext 保持为空（即匿名请求）。
最终响应 401：资源服务器的默认行为是拒绝访问并返回 HTTP 401 Unauthorized，响应通常会带上 WWW-Authenticate 头，类似 Bearer error="invalid_token", error_description="..."（其中描述会说明 token 已过期）。
可处理方式（选项）：
客户端应使用刷新令牌获取新的 access token；网关/下游返回 401 以提示刷新。
在网关侧提供自定义的错误响应或重定向（通过自定义的 AuthenticationEntryPoint / ServerAuthenticationEntryPoint）。
如果需要短时间容错，可在校验器中增加宽限（leeway），但这会降低严格性，应谨慎使用。
或在网关验证后将用户信息转成受信头转发并移除原始 Authorization（需配合内网信任策略）。
总结：过期的 JWT 会导致解码/校验失败，导致请求被当作未认证并返回 401；处理通常靠刷新令牌或在网关做自定义错误/转发策略
 */




