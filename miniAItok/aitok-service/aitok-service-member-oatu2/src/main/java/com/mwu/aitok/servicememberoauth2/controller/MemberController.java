
        package com.mwu.aitok.servicememberoauth2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member/a")
public class MemberController {

    @GetMapping
    public ResponseEntity<String> test(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request,
            Authentication authentication) {



        String username = jwt.getClaim("username");
        String userId = jwt.getClaim("userid");


        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication: " + authentication1.getPrincipal());

        Jwt jwt1 = ((JwtAuthenticationToken) authentication).getToken();
        System.out.println("jwt1: " + jwt1.getClaim("username"));
        System.out.println("jwt1: " + jwt1.getClaim("userid"));


        Jwt token = ((JwtAuthenticationToken) authentication).getToken();
        System.out.println("token00000: " + token.getClaim("username"));
        System.out.println("token0000: " + token.getClaim("userid"));



        // 如果 @AuthenticationPrincipal 没有注入 jwt，尝试从 Authentication 获取（防止 null）
        if (jwt == null && authentication instanceof JwtAuthenticationToken) {
            jwt = ((JwtAuthenticationToken) authentication).getToken();
        }

        Object idFromJwt = null;
        Object nameFromJwt = null;
        if (jwt != null) {
            idFromJwt = jwt.getClaim("userId");
            nameFromJwt = jwt.getClaim("username");
        } else {
            // 回退：使用网关注入的 headers（若网关已注入）
            idFromJwt = request.getHeader("X-User-Id");
            nameFromJwt = request.getHeader("X-Username");
        }

        if (idFromJwt == null && nameFromJwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("missing jwt and user headers");
        }

        idFromJwt = request.getHeader("X-User-Id");
        nameFromJwt = request.getHeader("X-Username");
        return ResponseEntity.ok("userId=" + idFromJwt + ", username=" + nameFromJwt);
    }
}
