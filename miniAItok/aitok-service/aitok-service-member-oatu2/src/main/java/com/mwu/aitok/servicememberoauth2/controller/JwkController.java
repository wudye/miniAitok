package com.mwu.aitok.servicememberoauth2.controller;

import com.mwu.aitok.servicememberoauth2.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member/test")
@RequiredArgsConstructor
public class JwkController {

    private final JwtService jwtService;

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getJwkSet() {
        return ResponseEntity.ok(jwtService.getJwkSet().toJSONObject());
    }
}