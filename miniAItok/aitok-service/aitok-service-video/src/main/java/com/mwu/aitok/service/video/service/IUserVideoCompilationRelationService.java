package com.mwu.aitok.service.video.service;

import com.mwu.aitok.model.video.domain.UserVideoCompilationRelation;
import com.mwu.aitok.model.video.dto.CompilationVideoPageDTO;
import com.mwu.aitok.model.video.vo.CompilationVideoVO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IUserVideoCompilationRelationService {

    /**
     * 将视频添加到合集
     *
     * @param videoId
     * @param compilationId
     * @return
     */
    Boolean videoRelateCompilation(String videoId, Long compilationId);

    /**
     * 删除视频
     *
     * @param videoId
     * @return
     */
    boolean deleteRecordByVideoId(String videoId);

    /**
     * 分页查询合集下视频
     *
     * @param pageDTO
     * @return
     */
    Page<UserVideoCompilationRelation> compilationVideoPage(CompilationVideoPageDTO pageDTO);

    /**
     * 合集视频分页查询
     * @param pageDTO
     * @return
     */
    List<CompilationVideoVO> compilationVideoPageList(CompilationVideoPageDTO pageDTO);
    Long compilationVideoPageCount(Long compilationId);

    Long getCompilationIdByVideoId(String videoId);
}
