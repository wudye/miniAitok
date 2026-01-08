package com.mwu.aitokservice.ai.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;


public class GetUserId {

    public static Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
            String userIda = jwt.getClaim("userid");
            Long userId = Long.parseLong(userIda);
            return userId;
        }
        return null; // Or throw an exception if user ID is always expected
    }
}

