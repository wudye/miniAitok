package com.mwu.aitok.service.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static com.mwu.aitok.service.security.CorUrlConstant.*;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final MemberUserDetailsServiceImpl accountDetails;



    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain
    ) throws ServletException, IOException, java.io.IOException {




        String path =request.getRequestURI();
        if (Arrays.asList(API_URLS_ALLOWED_USER).contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (Arrays.asList(FOR_OAUT2).contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }


        if (Arrays.asList(API_URLS_ALLOWED).contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }


        /*
            WebSocket 客户端连接时，浏览器会发起 HTTP Upgrade 请求，头部包含：
            Upgrade: websocket
            Connection: Upgrade
            Sec-WebSocket-Key: ...
            Sec-WebSocket-Protocol: ...
         */


        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);



            if (jwtUtils.validateToken(token)) {
                String userAccount = jwtUtils.getUserAccountFromToken(token);

                /*
                在你的 Spring Boot 项目中，使用 Authentication（比如通过 SecurityContextHolder.getContext().getAuthentication() 获取）时，通常可以拿到 UserDetails 对象和权限（role）。
具体来说：
getPrincipal() 返回的是 UserDetails 实例（比如你的 UserProfileDetails），里面包含用户名、角色、密码等信息。
getAuthorities() 返回的是权限（role）集合。
                 */
                UserDetails userDetails = accountDetails.loadUserByUsername(userAccount);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 将认证信息存入 SecurityContext
                /*
                SecurityContextHolder.getContext().setAuthentication(authToken); 这行代码的作用是将当前用户的认证信息（authToken）存入 Spring Security 的安全上下文。这样，后续在控制器或服务层就可以通过安全上下文获取当前用户的信息。
举个例子：
假设你有一个登录后的请求，JWT 校验通过后，代码会执行上述语句。此时，Spring Security 会认为当前线程已经有了认证用户。
在控制器里，你可以这样获取当前用户信息：
@GetMapping("/profile")
public ResponseEntity<?> getProfile(Authentication authentication) {
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    String email = userDetails.getUsername();
    Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();
    // 返回用户信息
    return ResponseEntity.ok(Map.of("email", email, "roles", roles));
}

                 */
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);



    }
}
