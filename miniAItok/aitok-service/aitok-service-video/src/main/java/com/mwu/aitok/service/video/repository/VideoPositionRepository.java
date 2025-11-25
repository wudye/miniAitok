package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.VideoPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoPositionRepository extends JpaRepository<VideoPosition, String> {
    VideoPosition findByVideoId(String videoId);

    void deleteByVideoId(String videoId);
}
