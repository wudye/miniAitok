package com.mwu.atiokservice.behave.repository;

import com.mwu.aitok.model.behave.domain.VideoUserComment;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoUserCommentRepository extends JpaRepository<VideoUserComment, Long> {
    VideoUserComment findByUserIdAndCommentId(Long userId, Long commentId);

    VideoUserComment findByCommentId(Long commentId);

    void deleteByCommentId(Long commentId);

    Page<VideoUserComment> findAllByVideoIdAndParentIdAndStatus(@NotNull String videoId, int i, String code, Pageable pageable);

    Long countByVideoIdAndStatus(String videoId, String code);

    List<VideoUserComment> findByOriginIdAndStatus(Long commentId, String code);

    void deleteByVideoId(String videoId);

    List<String> getVideoUserCommentByUserId(Long userId);

    Page<VideoUserComment> findAllByCommentId(Long commentId);

    Page<VideoUserComment> findAllByOriginIdAndStatus(Long originId, String status, Pageable pageable);

    Long countVideoUserCommentByOriginIdAndStatus(Long originId, String status);

    VideoUserComment findByParentId(Long parentId);
}
