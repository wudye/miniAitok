package com.mwu.aitok.service.video.repository;

import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.vo.app.VideoRecommendVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long>,  JpaSpecificationExecutor<Video> {





    List<Video> findAllByUserId(Long userId);

    List<Video> findAllByDelFlag(String delFlag);

    Video findByIdAndUserId(Long id, Long userId);

    List<Video> findByCreateTimeGreaterThanEqualAndDelFlag(LocalDateTime ctime, String code, Sort createTime);

    Long getLikeNumById(Long id);

    Long getCommentNumById(Long id);
    @Query("select coalesce(sum(v.likeNum), 0) from Video v where v.userId = :userId and v.delFlag = :delFlag")
    Long sumLikeNumByUserIdAndDelFlag(@Param("userId") Long userId, @Param("delFlag") String delFlag);

    Long countByUserIdAndDelFlag(Long userId, String code);



    Long getLikeCountById(Long id);

    Long getFavoritesCountById(Long id);

    Page<Video> findAllByIdIn(Collection<Long> ids, Pageable pageable);

    int countByUserIdAndId(Long userId, Long id);

    int getFavoritesByUserIdAndId(Long userId, Long id);

    int getLikeNumByUserIdAndId(Long userId, Long id);

    List<Video> findAllByIdIn(Collection<Long> ids, Sort sort);

    List<Video> findByIdInAndDelFlagOrderByCreateTimeDesc(Collection<Long> id, String delFlag);
}
