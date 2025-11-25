package com.mwu.atiokservice.behave.controller.app;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.behave.domain.VideoUserComment;
import com.mwu.aitok.model.behave.dto.VideoCommentReplayPageDTO;
import com.mwu.aitok.model.behave.dto.VideoUserCommentPageDTO;
import com.mwu.atiokservice.behave.service.IVideoUserCommentService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 视频评论
 *
 * @AUTHOR: roydon
 * @DATE: 2024/4/4
 **/
@RestController
@RequestMapping("/api/v1/app/comment")
public class AppVideoUserCommentController {

    @Resource
    private IVideoUserCommentService videoUserCommentService;

    /**
     * 获取视频父评论
     */
    @PostMapping("/parent")
    public PageData queryCommentParentPage(@Validated @RequestBody VideoUserCommentPageDTO pageDTO) {
        return videoUserCommentService.getCommentParentPage(pageDTO);
    }

    /**
     * 评论视频
     */
    @PostMapping
    public R<Boolean> commentVideo(@Validated @RequestBody VideoUserComment videoUserComment) {
        return R.ok(videoUserCommentService.commentVideo(videoUserComment));
    }

    /**
     * 分页评论回复
     */
    @PostMapping("/replyPage")
    public PageData queryCommentReplyPage(@Validated @RequestBody VideoCommentReplayPageDTO pageDTO) {
        return videoUserCommentService.getCommentReplyPage(pageDTO);
    }

}
