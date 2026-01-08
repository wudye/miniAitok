package com.mwu.aitok.service.video.repository;

import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import com.mwu.aitok.model.video.domain.UserVideoCompilationRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVideoCompilationRepository extends JpaRepository<UserVideoCompilation, Long>, JpaSpecificationExecutor<UserVideoCompilation> {
    Long countByCompilationId(Long compilationId);

    Page<UserVideoCompilation> findAllByCompilationId(Long compilationId, Pageable pageable);

    // from creator
    /**
     * 分页查询视频合集
     */
    @Query("SELECT uvc FROM UserVideoCompilation uvc WHERE " +
            "uvc.userId = :userId AND " +
            "(:title IS NULL OR uvc.title LIKE %:title%) " +
            "ORDER BY uvc.createTime DESC")
    Page<UserVideoCompilation> selectVideoCompilationPage(
            @Param("userId") Long userId,
            @Param("title") String title,
            Pageable pageable
    );

    /**
     * 统计视频合集数量
     */
    @Query("SELECT COUNT(uvc) FROM UserVideoCompilation uvc WHERE " +
            "uvc.userId = :userId AND " +
            "(:title IS NULL OR uvc.title LIKE %:title%)")
    long selectVideoCompilationPageCount(
            @Param("userId") Long userId,
            @Param("title") String title
    );



}
