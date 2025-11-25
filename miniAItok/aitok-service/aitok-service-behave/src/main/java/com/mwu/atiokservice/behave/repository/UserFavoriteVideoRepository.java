package com.mwu.atiokservice.behave.repository;

import com.mwu.aitok.model.behave.domain.UserFavoriteVideo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavoriteVideoRepository  extends JpaRepository<UserFavoriteVideo, Long> {
    void deleteByUserIdAndVideoId(Long userId, String videoId);

    Page<UserFavoriteVideo> findAllByUserId(Long userId, Pageable pageable);

    void deleteByVideoId(String videoId);
}
