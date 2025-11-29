package com.mwu.aitokservice.notice.controller.app;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.notice.dto.NoticePageDTO;
import com.mwu.aitokservice.notice.service.INoticeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 通知表(Notice)表控制层
 *
 * @author mwu
 * @since 2023-11-08 16:21:43
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/app")
public class AppNoticeController {

    @Resource
    private INoticeService noticeService;

    /**
     * 未读消息数量
     */
    @GetMapping("/unreadCount")
    public R<Long> unReadNoticeCount() {
        return R.ok(noticeService.getUnreadNoticeCount());
    }

    /**
     * 分页根据条件查询行为通知
     *
     * @param pageDTO
     * @return
     */
    @PostMapping("/behavePage")
    public PageData behaveNoticePage(@RequestBody NoticePageDTO pageDTO) {
        return noticeService.getBehaveNoticePage(pageDTO);
    }


}

