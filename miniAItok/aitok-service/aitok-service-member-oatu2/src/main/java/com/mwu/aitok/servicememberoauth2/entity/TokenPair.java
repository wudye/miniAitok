package com.mwu.aitok.servicememberoauth2.entity;

import java.time.Instant;

public record TokenPair(String accessToken, String refreshToken, Instant accessExpiry, Instant refreshExpiry) {

}