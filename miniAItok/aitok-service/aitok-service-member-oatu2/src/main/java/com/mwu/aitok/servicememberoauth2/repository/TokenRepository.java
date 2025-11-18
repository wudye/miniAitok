package com.mwu.aitok.servicememberoauth2.repository;



import com.mwu.aitok.servicememberoauth2.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<TokenEntity, Long> {
    Optional<TokenEntity> findByUserId(String userId);
    Optional<TokenEntity> findByRefreshToken(String refreshToken);
}
