package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.social.domain.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {


    double countByUserIdAndUserFollowId(Long userId, Long userFollowId);
}
