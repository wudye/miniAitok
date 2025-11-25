package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.UserVideoCompilationRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVideoCompilationRelationRepository extends JpaRepository<UserVideoCompilationRelation, Long> {
    void deleteByVideoId(String videoId);

    Page<UserVideoCompilationRelation> findByCompilationId(Long compilationId);

    Page<UserVideoCompilationRelation> findByCompilationId(Long compilationId, Pageable pageable);

    Long countByCompilationId(Long compilationId);

    List<UserVideoCompilationRelation> findByVideoId(String videoId);
}
