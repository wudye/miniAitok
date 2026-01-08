package com.mwu.aitokservice.creator.repository;

import com.mwu.aitok.model.creator.dto.videoCompilationPageDTO;
import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVideoCompilationRepository extends JpaRepository<UserVideoCompilation, Long> {
    
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