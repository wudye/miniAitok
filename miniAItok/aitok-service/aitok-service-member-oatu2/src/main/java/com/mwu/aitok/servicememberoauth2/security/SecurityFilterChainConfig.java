package com.mwu.aitok.servicememberoauth2.security;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilterChainConfig {
    @Value("${oauth2.resourceserver.jwt.jwk-set-uri:http://localhost:18002/member/test/.well-known/jwks.json}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 放行所有 OPTIONS

                        .requestMatchers("/**").permitAll()

                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authenticationProvider(authenticationProvider)
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(except -> except
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            HttpSession session = request.getSession(false);
//                            if (session == null || session.getAttribute("username") == null) {
//                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                                System.out.println("未登录或会话已过期: session=" + session);
//                            }
//                        })
//                );
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
                ;//.exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint));
        return http.build();


    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }
}