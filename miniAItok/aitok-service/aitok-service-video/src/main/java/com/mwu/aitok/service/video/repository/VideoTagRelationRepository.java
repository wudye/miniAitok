package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.VideoTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoTagRelationRepository extends JpaRepository<VideoTagRelation,String> {
    void deleteByVideoId(String videoId);

    List<VideoTagRelation> findByVideoId(String videoId);
}
