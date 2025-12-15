//package com.mwu.aitokservice.creator.security;
//
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class UserIdResponseHeaderFilter extends OncePerRequestFilter {
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        // 先执行后续过滤器
//        filterChain.doFilter(request, response);
//
//        // 获取当前认证信息
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication != null && authentication.isAuthenticated()) {
//            String userId = null;
//
//            // 从JWT中提取userId
//            if (authentication instanceof JwtAuthenticationToken) {
//                Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
//                userId = jwt.getClaim("userid"); // 对应claims.put("userid", userId)
//
//                if (userId != null) {
//                    // 添加到响应头
//                    response.setHeader("X-User-Id", userId);
//                    log.debug("Added X-User-Id header: {}", userId);
//                }
//            }
//        }
//    }
//}
