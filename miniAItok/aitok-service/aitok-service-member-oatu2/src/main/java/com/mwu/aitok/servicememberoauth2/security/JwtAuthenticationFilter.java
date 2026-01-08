package com.mwu.aitok.servicememberoauth2.security;


import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.servicememberoauth2.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public class JwtAuthenticationFilter {

/*
    private final MemberRepository memberRepository;

    public JwtAuthenticationFilter(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken jwtAuth && jwtAuth.isAuthenticated()) {
            String username = jwtAuth.getToken().getClaimAsString("username");
            String userId = jwtAuth.getToken().getClaimAsString("userid");
            Optional<Member> memberOpt = memberRepository.findByUserIdAndUserName(Long.parseLong(userId), username);
            if (memberOpt.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户不存在");
                return;
            }
            Member member = memberOpt.get();
            if (member.getDelFlag() == "1") { // 假设 Member 有 isEnabled 方法
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户已禁用");
                return;
            }

            // 保留原 authorities（从 JWT 提取），并把 principal 换成 DB 中的 Member
            Collection<? extends GrantedAuthority> authorities = jwtAuth.getAuthorities();
            // 新的 Authentication：principal 放 Member，credentials 置为 null
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken newAuth =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(member, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        filterChain.doFilter(request, response);
    }

 */

}
