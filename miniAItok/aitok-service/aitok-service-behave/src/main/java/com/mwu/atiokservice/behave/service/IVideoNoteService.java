package com.mwu.atiokservice.behave.service;


import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.behave.dto.VideoNotePageDTO;
import com.mwu.aitok.model.behave.vo.VideoNoteVO;

/**
 * 视频笔记表(VideoNote)表服务接口
 *
 * @author roydon
 * @since 2024-05-05 18:51:05
 */
public interface IVideoNoteService  {

    /**
     * 分页
     * @param pageDTO
     * @return
     */
    PageData<VideoNoteVO> queryVideoNotePage(VideoNotePageDTO pageDTO);

}
