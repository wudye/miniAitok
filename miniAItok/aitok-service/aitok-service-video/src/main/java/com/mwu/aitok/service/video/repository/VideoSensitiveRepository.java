package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.VideoSensitive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoSensitiveRepository extends JpaRepository<VideoSensitive, String> {
}
