package com.mwu.aitok.servicememberoauth2.repository;



import com.mwu.aitok.servicememberoauth2.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<TokenEntity, Long> {
    Optional<TokenEntity> findByUserId(String userId);
    Optional<TokenEntity> findByRefreshToken(String refreshToken);
    @Modifying
    @Transactional
    @Query("delete from TokenEntity t where t.refreshExpiry < :cutoff")
    int deleteByRefreshExpiryBefore(Instant now);
}
