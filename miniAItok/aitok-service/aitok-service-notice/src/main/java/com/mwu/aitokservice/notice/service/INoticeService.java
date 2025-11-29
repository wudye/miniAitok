package com.mwu.aitokservice.notice.service;


import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.notice.domain.Notice;
import com.mwu.aitok.model.notice.dto.NoticePageDTO;
import org.springframework.data.domain.Page;

/**
 * 通知表(Notice)表服务接口
 *
 * @author mwu
 * @since 2023-11-08 16:21:45
 */
public interface INoticeService{

    /**
     * 分页查询用户通知
     *
     * @param pageDTO
     * @return
     */
    Page<Notice> queryUserNoticePage(NoticePageDTO pageDTO);

    /**
     * 获取未读消息数量
     */
    Long getUnreadNoticeCount();

    /**
     * 分页行为通知
     *
     * @param pageDTO
     * @return
     */
    PageData getBehaveNoticePage(NoticePageDTO pageDTO);

    /**
     * 新增消息
     *
     * @param notice
     * @return
     */
    boolean saveNotice(Notice notice);
}
