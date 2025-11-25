package com.mwu.aitokservice.recommend.repository;

import com.mwu.aitok.model.behave.domain.UserVideoBehave;
import com.mwu.aitok.model.recommend.modal.UserVideoScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVideoBehaveRepository extends JpaRepository<UserVideoBehave, Long> {
    @Query(
            value = "SELECT user_id AS userId, video_id AS videoId, SUM(user_behave) AS score " +
                    "FROM user_video_behave " +
                    "GROUP BY user_id, video_id",
            nativeQuery = true
    )
    List<UserVideoScore> queryAllUserVideoBehave();
}
