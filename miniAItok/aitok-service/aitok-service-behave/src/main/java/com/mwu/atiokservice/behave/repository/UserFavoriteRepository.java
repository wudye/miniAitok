package com.mwu.atiokservice.behave.repository;

import com.mwu.aitok.model.behave.domain.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    Page<UserFavorite> findAllByUserId(Long userId, Pageable pageable);

    List<UserFavorite> findByUserId(Long userId);

    Long countById(Long id);

    List<Long> getUserFavoriteById(Long id);

    List<Long> findByIdOrderByCreateTimeDesc(Long id);

    UserFavorite findByUserIdAndId(Long userId, Long id);
}
