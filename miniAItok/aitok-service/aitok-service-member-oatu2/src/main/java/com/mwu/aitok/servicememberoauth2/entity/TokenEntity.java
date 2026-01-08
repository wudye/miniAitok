package com.mwu.aitok.servicememberoauth2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tokens")
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 唯一约束仅限此主键

    @Column(name = "user_id", nullable = false)
    private String userId; // 非唯一

    @Column(name = "username", nullable = true)
    private String username; // 非唯一

    @Lob
    @Column(name = "refresh_token", nullable = false, columnDefinition = "LONGTEXT", unique = true)
    private String refreshToken; // refresh token 保持唯一

    @Column(name = "refresh_expiry")
    private Instant refreshExpiry;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
