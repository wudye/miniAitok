package com.mwu.aitok.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/fallback/account")
    public ResponseEntity<String> accountFallback() {
        return ResponseEntity.status(503).body("account service unavailable - fallback");
    }
    @GetMapping("/fallback/default")
    public ResponseEntity<String> defaultFallback() {
        return ResponseEntity.status(503).body("default for all  service unavailable - fallback");
    }
}