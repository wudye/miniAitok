package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.VideoImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoImageRepository extends JpaRepository<VideoImage , Long> {
    List<VideoImage> findByVideoId(String videoId);

    void deleteByVideoId(String videoId);
}
