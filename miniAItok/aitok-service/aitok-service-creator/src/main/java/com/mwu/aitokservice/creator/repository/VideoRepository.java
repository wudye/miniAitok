package com.mwu.aitokservice.creator.repository;

import com.mwu.aitok.model.creator.dto.VideoPageDTO;
import com.mwu.aitok.model.video.domain.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    
    Page<Video> findAllByUserId(Long userId, Pageable pageable);

    /**
     * 分页查询视频 - 使用 JPQL
     */
    @Query("SELECT v FROM Video v WHERE " +
           "v.userId = :userId AND v.delFlag = '0' AND " +
           "(:videoTitle IS NULL OR v.videoTitle LIKE %:videoTitle%) AND " +
           "(:publishType IS NULL OR v.publishType = :publishType) AND " +
           "(:showType IS NULL OR v.showType = :showType) AND " +
           "(:positionFlag IS NULL OR v.positionFlag = :positionFlag) AND " +
           "(:auditsStatus IS NULL OR v.auditsStatus = :auditsStatus) " +
           "ORDER BY v.createTime DESC")
    Page<Video> selectVideoPage(
        @Param("userId") Long userId,
        @Param("videoTitle") String videoTitle,
        @Param("publishType") String publishType,
        @Param("showType") String showType,
        @Param("positionFlag") String positionFlag,
        @Param("auditsStatus") String auditsStatus,
        Pageable pageable
    );

    /**
     * 统计视频数量
     */
    @Query("SELECT COUNT(v) FROM Video v WHERE " +
           "v.userId = :userId AND v.delFlag = '0' AND " +
           "(:videoTitle IS NULL OR v.videoTitle LIKE %:videoTitle%) AND " +
           "(:publishType IS NULL OR v.publishType = :publishType) AND " +
           "(:showType IS NULL OR v.showType = :showType) AND " +
           "(:positionFlag IS NULL OR v.positionFlag = :positionFlag) AND " +
           "(:auditsStatus IS NULL OR v.auditsStatus = :auditsStatus)")
    long selectVideoPageCount(
        @Param("userId") Long userId,
        @Param("videoTitle") String videoTitle,
        @Param("publishType") String publishType,
        @Param("showType") String showType,
        @Param("positionFlag") String positionFlag,
        @Param("auditsStatus") String auditsStatus
    );

    @Query("select coalesce(sum(v.viewNum), 0) from Video v where v.userId = :userId and v.delFlag = '0'")
    Long selectVideoPlayAmount(Long userId);

    @Query(value = "SELECT COALESCE(SUM(CASE " +
            "  WHEN DATE(v.create_time) > DATE_SUB(CURDATE(), INTERVAL 7 DAY) THEN v.view_num " +
            "END), 0) AS view_growth " +
            "FROM video v " +
            "WHERE v.user_id = :userId AND v.del_flag = '0'",
            nativeQuery = true)
    Long selectVideoPlayAmountAdd(Long userId);

    @Query(value =
            "WITH RECURSIVE dates AS ( " +
                    "  SELECT CURDATE() - INTERVAL 6 DAY AS day " +
                    "  UNION ALL " +
                    "  SELECT day + INTERVAL 1 DAY FROM dates WHERE day < CURDATE() " +
                    ") " +
                    "SELECT COALESCE(SUM(v.view_num), 0) AS total_views " +
                    "FROM dates d " +
                    "LEFT JOIN video v " +
                    "  ON DATE(v.create_time) = d.day " +
                    " AND v.user_id = :userId " +
                    " AND v.del_flag = '0' " +
                    "GROUP BY d.day " +
                    "ORDER BY d.day",
            nativeQuery = true)
    List<Long> selectVideoPlayAmount7Day(Long userId);

    @Query(value = "SELECT COUNT(1) AS count FROM user_follow WHERE user_follow_id = :userId", nativeQuery = true)
    Long selectFansAmount(@Param("userId") Long userId);

    // Java
    @Query(value = "SELECT COUNT(*) AS follower_growth " +
            "FROM user_follow " +
            "WHERE user_follow_id = :userId " +
            "  AND create_time BETWEEN DATE_SUB(CURDATE(), INTERVAL 7 DAY) AND CURDATE()",
            nativeQuery = true)
    Long selectFansAmountAdd(@Param("userId") Long userId);


    @Query("select coalesce(sum(v.likeNum), 0) from Video v where v.userId = :userId and v.delFlag = '0'")
    Long selectVideoLikeAmount(@Param("userId") Long userId);

    @Query(value = "SELECT COALESCE(SUM(CASE " +
            "  WHEN DATE(v.create_time) > DATE_SUB(CURDATE(), INTERVAL 7 DAY) THEN v.like_num " +
            "END), 0) AS like_growth " +
            "FROM video v " +
            "WHERE v.user_id = :userId AND v.del_flag = '0'",
            nativeQuery = true)
    Long selectVideoLikeAmountAdd(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) AS count " +
            "FROM video_user_comment vuc " +
            "WHERE vuc.video_id IN (SELECT v.video_id FROM video v WHERE v.user_id = :userId)",
            nativeQuery = true)
    Long selectVideoCommentAmount(@Param("userId") Long userId);

    @Query(value = "SELECT COUNT(*) AS count " +
            "FROM video_user_comment vuc " +
            "WHERE vuc.user_id = :userId " +
            "  AND vuc.create_time BETWEEN DATE_SUB(CURDATE(), INTERVAL 7 DAY) AND CURDATE()",
            nativeQuery = true)
    Long selectVideoCommentAmountAdd(@Param("userId") Long userId);

    @Query(value =
            "WITH RECURSIVE dates AS ( " +
                    "  SELECT CURDATE() - INTERVAL 6 DAY AS day " +
                    "  UNION ALL " +
                    "  SELECT day + INTERVAL 1 DAY FROM dates WHERE day < CURDATE() " +
                    ") " +
                    "SELECT COALESCE(COUNT(uf.user_id), 0) AS follower_growth " +
                    "FROM dates d " +
                    "LEFT JOIN user_follow uf " +
                    "  ON d.day = DATE(uf.create_time) " +
                    " AND uf.user_follow_id = :userId " +
                    "GROUP BY d.day " +
                    "ORDER BY d.day",
            nativeQuery = true)
    List<Long> selectFansAmount7Day(@Param("userId") Long userId);
    @Query(value =
            "WITH RECURSIVE dates AS ( " +
                    "  SELECT CURDATE() - INTERVAL 6 DAY AS day " +
                    "  UNION ALL " +
                    "  SELECT day + INTERVAL 1 DAY FROM dates WHERE day < CURDATE() " +
                    ") " +
                    "SELECT COALESCE(SUM(v.like_num), 0) AS total_likes " +
                    "FROM dates d " +
                    "LEFT JOIN video v " +
                    "  ON DATE(v.create_time) = d.day " +
                    " AND v.user_id = :userId " +
                    " AND v.del_flag = '0' " +
                    "GROUP BY d.day " +
                    "ORDER BY d.day",
            nativeQuery = true)
    List<Long> selectVideoLikeAmount7Day(@Param("userId") Long userId);
    @Query(value =
            "WITH RECURSIVE dates AS ( " +
                    "  SELECT CURDATE() - INTERVAL 6 DAY AS day " +
                    "  UNION ALL " +
                    "  SELECT day + INTERVAL 1 DAY FROM dates WHERE day < CURDATE() " +
                    ") " +
                    "SELECT COALESCE(COUNT(vuc.id), 0) AS comment_count " +
                    "FROM dates d " +
                    "LEFT JOIN video_user_comment vuc " +
                    "  ON DATE(vuc.create_time) = d.day " +
                    " AND vuc.user_id = :userId " +
                    "GROUP BY d.day " +
                    "ORDER BY d.day",
            nativeQuery = true)
    List<Long> selectVideoCommentAmount7Day(@Param("userId") Long userId);}
