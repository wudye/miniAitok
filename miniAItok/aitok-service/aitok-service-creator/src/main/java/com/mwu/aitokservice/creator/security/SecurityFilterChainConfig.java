package com.mwu.aitokservice.creator.security;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityFilterChainConfig {
    @Value("${oauth2.resourceserver.jwt.jwk-set-uri:http://localhost:18002/member/test/.well-known/jwks.json}")
    private String jwkSetUri;


    /*
    private final MemberRepository memberRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider(memberRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        // AuthenticationConfiguration 会自动包含注册的 AuthenticationProvider beans
        return cfg.getAuthenticationManager();
    }

     */


/*
oauth2ResourceServer().jwt() 做的事：验证 Authorization: Bearer <JWT> 的完整性与合法性（签名/过期/claims），并生成一个 JwtAuthenticationToken（或用 JwtAuthenticationConverter 转换权限）。
它不做的事：不会检查“当初登录时提交的用户名/密码是否正确”（那是签发 token 时的责任），也默认不检查数据库里的账号状态（除非你在 token->Authentication 的转换里主动去查 DB 并做额外校验）。
登录和签发 token 必须做凭证校验：如果你提供用户名/密码登录端点，必须在签发 JWT 前使用 AuthenticationManager/UserDetailsService/PasswordEncoder 或自定义 AuthenticationProvider 去校验凭证、账号状态等。
何时可以删掉 UserDetailsService/PasswordEncoder：只有当后端仅作 Resource Server（不签发 token，也不需要在请求时从 DB 校验额外用户状态）时，才可以省略这些组件。
如果希望在每次请求基于 token 再次校验账号（比如检查用户是否被禁用），可以在 JwtAuthenticationConverter 或自定义过滤器中，从 token 的 sub 去 DB 拉用户并做检查。
一句话：通过 JWT 的验证说明 token 本身被信任，但这并不替代登录时的用户名/密码校验，登录时必须先验证凭证才能安全签发 JW
 */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        http
                /*
                in prodiction, enable CSRF protection for state-changing operations should be httponly
                .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(
                            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/v1/login"),
                            new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/v1/register")
                    )
                 )

                 */
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(   "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/v2/api-docs").permitAll()
                        .anyRequest().authenticated()

                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
//                  .addFilterAfter(userIdResponseHeaderFilter,
//                org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter.class);



        return http.build();


    }

    @Bean
    public JwtDecoder jwtDecoder() {

        JwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri)
                .build();

        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                Jwt jwt = decoder.decode(token);
                log.info("JWT claims: {}", jwt.getClaims());
                return decoder.decode(token);
            }
        };
    }


}