package com.mwu.aitok.service.video.service;

import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import com.mwu.aitok.model.video.dto.CompilationVideoPageDTO;
import com.mwu.aitok.model.video.dto.UpdateUserVideoCompilationDTO;
import com.mwu.aitok.model.video.dto.UserVideoCompilationPageDTO;
import com.mwu.aitok.model.video.vo.UserVideoCompilationInfoVO;
import org.springframework.data.domain.Page;

public interface IUserVideoCompilationService {
    /**
     * 分页查询我的合集
     *
     * @param pageDTO
     * @return
     */
    Page<UserVideoCompilation> videoCompilationMyPage(UserVideoCompilationPageDTO pageDTO);

    /**
     * 分页查询用户合集
     *
     * @param pageDTO
     * @return
     */
    Page<UserVideoCompilation> videoCompilationUserPage(UserVideoCompilationPageDTO pageDTO);

    /**
     * 合集播放量
     */
    Long compilationViewCount(Long compilationId);

    /**
     * 获赞量
     */
    Long compilationLikeCount(Long compilationId);

    /**
     * 被收藏数
     */
    Long compilationFavoriteCount(Long compilationId);

    /**
     * 视频数
     */
    Long compilationVideoCount(Long compilationId);

    /**
     * 更新视频合集
     */
    Boolean updateVideoCompilationInfo(UpdateUserVideoCompilationDTO updateUserVideoCompilationDTO);

    /**
     * 合集视频分页
     */
    Page<UserVideoCompilation> compilationVideoPage(CompilationVideoPageDTO pageDTO);

    /**
     * 根据视频id获取合集信息
     *
     * @param videoId
     * @return
     */
    UserVideoCompilationInfoVO getCompilationInfoVOByVideoId(String videoId);
}
