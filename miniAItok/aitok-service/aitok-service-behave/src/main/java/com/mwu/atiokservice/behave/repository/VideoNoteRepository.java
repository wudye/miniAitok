package com.mwu.atiokservice.behave.repository;

import com.mwu.aitok.model.behave.domain.VideoNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoNoteRepository  extends JpaRepository<VideoNote, Long> {
    Page<VideoNote> findAllByVideoIdAndDelFlag(String videoId, String delFlag, Pageable pageable);
}
