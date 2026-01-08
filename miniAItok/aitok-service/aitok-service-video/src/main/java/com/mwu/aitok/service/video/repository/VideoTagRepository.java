package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.VideoTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoTagRepository extends JpaRepository<VideoTag,Long> {
}
