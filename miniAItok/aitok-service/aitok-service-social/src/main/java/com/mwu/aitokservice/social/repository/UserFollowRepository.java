package com.mwu.aitokservice.social.repository;

import com.mwu.aitok.model.social.domain.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFollowRepository extends


        JpaRepository<UserFollow, Long> {
    List<UserFollow> findAllByUserIdAndUserFollowId(Long userId, Long userFollowId);

    void deleteByUserIdAndUserFollowId(Long userId, Long userFollowId);

    Page<UserFollow> findAllByUserId(Long userId, Pageable pageable);

    List<UserFollow> findAllByUserId(Long userId);

    Integer countByUserIdAndUserFollowId(Long userId, Long followUserId);

    Long countByUserId(Long userId);

    Long countByUserIdOrUserFollowId(Long userId, Long userFollowId);

    Long countByUserFollowId(Long userFollowId);
}
