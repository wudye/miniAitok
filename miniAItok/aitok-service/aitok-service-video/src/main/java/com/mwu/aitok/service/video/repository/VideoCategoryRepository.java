package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.VideoCategory;
import jakarta.annotation.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoCategoryRepository extends JpaRepository<VideoCategory, Long> {
    List<VideoCategory> findByStatus(String status);

    List<VideoCategory> findByStatusAndParentId(String status, Long parentId);

    List<VideoCategory> findByParentIdAndStatusOrderByOrderNumAsc(Long id, String code);

    List<VideoCategory> findByStatusAndVisibleAfterOrderByOrderNumAsc(String status, String visibleAfter);

    List<VideoCategory> findByIdOrParentId(Long id, Long parentId);

    List<VideoCategory> findAllByIdOrParentId(Long id, Long parentId);

    @Query("select count(vcr) from VideoCategoryRelation vcr " +
            "where vcr.categoryId = :categoryId " +
            "or vcr.categoryId in (select vc.id from VideoCategory vc where vc.parentId = :categoryId)")
    Long countByCategoryOrChildren(Long categoryId);
}
