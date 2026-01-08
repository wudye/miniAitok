package com.mwu.aitok.service.config;

import com.mwu.aitok.service.security.DelegatedAuthEntryPoint;
import com.mwu.aitok.service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilterChainConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    private final DelegatedAuthEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 放行所有 OPTIONS

                        .requestMatchers("/member/test/**").permitAll()

                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(except -> except
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            HttpSession session = request.getSession(false);
//                            if (session == null || session.getAttribute("username") == null) {
//                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                                System.out.println("未登录或会话已过期: session=" + session);
//                            }
//                        })
//                );
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint));
        return http.build();


    }
}