package com.mwu.atiokservice.behave.repository;

import com.mwu.aitok.model.behave.domain.VideoUserFavorites;
import com.mwu.aitok.model.behave.vo.UserFavoriteVideoVO;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.dto.VideoPageDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoUserFavoritesRepository extends JpaRepository<VideoUserFavorites, Long> {
    double countByVideoIdAndUserId(String videoId, Long userId);

    Video findByVideoId(String videoId);

    List<VideoUserFavorites> findByVideoIdAndUserId(String videoId, Long userId);

    void deleteByVideoId(String videoId);

    double countVideoUserFavoritesByUserIdAndVideoId(Long userId, String videoId);

    Long countVideoUserFavoritesByUserId(Long userId);

    // TODO need refactor
    List<UserFavoriteVideoVO> selectUserFavoriteVideos(VideoPageDto pageDto);

    // TODO need refactor
    Long selectUserFavoriteVideosCount(VideoPageDto pageDto);

    // TODO need refactor
    List<String> selectUserFavoriteVideoIds(VideoPageDto pageDto);

    Long countByVideoId(String videoId);

    List<VideoUserFavorites> findByUserId(Long userId);
}
