package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import com.mwu.aitok.model.video.domain.VideoCategoryRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoCategoryRelationRepository extends JpaRepository<VideoCategoryRelation, Long> {


    List<VideoCategoryRelation> findByVideoId(String videoId);

    void deleteByVideoId(String videoId);

    int countByCategoryId(Long categoryId);

    List<Long> getVideoIdByCategoryId(Long categoryId);


}
