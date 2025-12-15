package com.mwu.aitok.gatewayserver.config;



import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;


import org.springframework.security.web.server.SecurityWebFilterChain;


import org.springframework.web.cors.CorsConfiguration;


import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Resource
//    private JwtHeaderFilter jwtHeaderFilter;

    @Value("${oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.audience:}") // 可选
    private String audience;

//    @Resource
//    private  UserIdResponseHeaderFilter userIdResponseHeaderFilter;

    private static final String[] COMMON_PUBLIC = new String[]{
            "/*/actuator/**",
            "/*/v3/api-docs/**",
            "/*/swagger-ui/**",
            "/*/swagger-ui.html",
            "/*/api/v1/login", // 登录接口：/member/api/v1/login
            "/*/api/v1/register",
            "/*/api/v1/sms-login", // 短信登陆接口：/member/api/v1/sms-login
            "/*/register",
            "/*/app/sms-register",
            "/*/swagger-ui/**",
            "/*/api/v1/feed",
            "/*/api/v1/pushVideo",
            "/*/api/v1/app/recommend",
            "/*/api/v1/app/hotVideo",
            "/*/api/v1/app/video/hotSearch",
            "/*/api/v1/hot",
            "/*/api/v1/video/search/hot",
            "/*/websocket",
            "/*/userVideoBehave/syncViewBehave",
            "/*/api/v1/video/feed",
            "/*/api/v1/category/tree",
            "/*/api/v1/category/parentList",
            "/*/api/v1/category/children",
            "/*/api/v1/category/pushVideo",
            "/*/chat/stream",
            "/test"
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



    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults()) // 使用默认 CORS 配置或按需自定义
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(COMMON_PUBLIC).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
//                // 在这里添加你的过滤器
//                .addFilterAt(userIdResponseHeaderFilter, SecurityWebFiltersOrder.OAUTH2_AUTHORIZATION_CODE);





/*                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
                    jwt.jwtAuthenticationConverter(
                            new ReactiveJwtAuthenticationConverterAdapter(
                                    new CustomJwtAuthenticationConverter()
                            )
                    );
                }))




 */
        return http.build();
    }


    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {

        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        System.out.println("jwkSetUri: " + jwkSetUri);
        System.out.println("jwtDecoder: " + jwtDecoder.toString());

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
    /*
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            if (audience == null || audience.isBlank()) {
                return OAuth2TokenValidatorResult.success();
            }
            List<String> aud = jwt.getAudience();
            if (aud != null && aud.contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error err = new OAuth2Error("invalid_token", "The required audience is missing", null);
            return OAuth2TokenValidatorResult.failure(err);
        };

        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
        jwtDecoder.setJwtValidator(validator);

        return jwtDecoder;
    }


    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConv = new JwtAuthenticationConverter();
        jwtConv.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object roles = jwt.getClaim("roles");
            if (roles instanceof List) {
                return ((List<?>) roles).stream()
                        .map(Object::toString)
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());
            }
            return List.of();
        });
        ReactiveJwtAuthenticationConverterAdapter reactiveAdapter =
                new ReactiveJwtAuthenticationConverterAdapter(jwtConv);

        return jwtConv;
    }

     */
}
