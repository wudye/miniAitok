package com.mwu.atiokservice.behave.repository;

import com.mwu.aitok.model.behave.domain.VideoUserLike;
import com.mwu.aitok.model.member.domain.MemberInfo;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.domain.VideoImage;
import com.mwu.aitok.model.video.domain.VideoPosition;
import com.mwu.aitok.model.video.dto.VideoPageDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoUserLikeRepository extends JpaRepository<VideoUserLike, Long> {
    double countByVideoIdAndUserId(String videoId, Long userId);

    Long getLikeNumByVideoId(String videoId);

    Video findByVideoId(String videoId);

    void deleteByVideoId(String videoId);

    Long countByUserId(Long userId);

    VideoUserLike findByUserIdAndVideoId(Long userId, String videoId);

    void deleteByUserIdAndVideoId(Long userId, String videoId);

    List<Long> getVideoIdsByUserId(Long userId);
    @Query(value = "SELECT COUNT(1) FROM video_user_like vul " +
            "RIGHT JOIN video v ON vul.video_id = v.video_id " +
            "WHERE vul.user_id = :userId " +
            "AND (:videoTitle IS NULL OR v.video_title LIKE %:videoTitle%)", nativeQuery = true)
//    long countPersonLike(Long userId, String videoTitle);

    long selectPersonLikeCount(Long userId, String videoTitle);



    List<VideoUserLike> findByUserIdOrVideoId(Long userId, String videoId);

    List<VideoUserLike> findByUserId(Long userId);

    // TODO need refactor
    boolean userLikeVideo(String videoId, Long userId);
    // TODO need refactor
    List<Video> selectPersonLikePage(VideoPageDto pageDto);

    // TODO need refactor
    List<VideoImage> selectImagesByVideoId(String videoId);

    // TODO need refactor
    MemberInfo selectPersonLikeShowStatus(Long userId);

    // TODO need refactor
    VideoPosition selectPositionByVideoId(String videoId);

    // TODO need refactor
    Video selectVideoByVideoId(String videoId);

    // TODO need refactor
    List<VideoImage> selectImagesByVideoIds(List<String> imageVideoIds);

    VideoUserLike findVideoUserLikeByVideoId(String videoId);
}
