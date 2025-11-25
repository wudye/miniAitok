package com.mwu.atiokservice.behave.controller.v1;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.model.behave.domain.VideoUserComment;
import com.mwu.aitok.model.behave.dto.VideoUserCommentPageDTO;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import com.mwu.aitok.model.notice.domain.Notice;
import com.mwu.aitok.model.notice.enums.NoticeType;
import com.mwu.aitok.model.notice.enums.ReceiveFlag;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitolk.feign.member.RemoteMemberService;
import com.mwu.atiokservice.behave.repository.VideoUserCommentRepository;
import com.mwu.atiokservice.behave.repository.VideoUserLikeRepository;
import com.mwu.atiokservice.behave.service.IVideoUserCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_CREATE_ROUTING_KEY;
import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_DIRECT_EXCHANGE;


/**
 * (VideoUserComment)表控制层
 *
 * @author roydon
 * @since 2023-10-30 16:52:51
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/comment")
public class VideoUserCommentController {

    @Resource
    private IVideoUserCommentService videoUserCommentService;

    @Resource
    private VideoUserCommentRepository videoUserCommentRepository;

    @Resource
    private RemoteMemberService remoteMemberService;

    @Resource
    private RedisService redisService;

    @Resource
    private VideoUserLikeRepository videoUserLikeMapper;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 分页查询评论集合树
     */
    @PostMapping("/tree")
    public PageData queryTree(@RequestBody VideoUserCommentPageDTO pageDTO) {
        return videoUserCommentService.getCommentPageTree(pageDTO);
    }

    /**
     * 新增评论
     */
    @PostMapping
    public R<?> add(@RequestBody VideoUserComment videoUserComment) throws JsonProcessingException {
        if (StringUtils.isNull(videoUserComment.getContent()) || StringUtils.isBlank(videoUserComment.getContent())) {
            throw new CustomException(HttpCodeEnum.COMMENT_CONTENT_NULL);
        }
        videoUserComment.setCreateTime(LocalDateTime.now());
        videoUserComment.setUserId(UserContext.getUser().getUserId());
        sendNotice2MQ(videoUserComment.getVideoId(), videoUserComment.getContent(), UserContext.getUser().getUserId());
        this.videoUserCommentRepository.save(videoUserComment);
        return R.ok("评论成功");
    }

    /**
     * 用户评论视频，通知mq
     *
     * @param videoId
     * @param operateUserId
     */
    private void sendNotice2MQ(String videoId, String content, Long operateUserId) throws JsonProcessingException {
        // 根据视频获取发布者id
        Video video = videoUserLikeMapper.findByVideoId(videoId);
        if (StringUtils.isNull(video)) {
            return;
        }
        if (operateUserId.equals(video.getUserId())) {
            return;
        }
        // 封装notice实体
        Notice notice = new Notice();
        notice.setOperateUserId(operateUserId);
        notice.setNoticeUserId(video.getUserId());
        notice.setVideoId(videoId);
        notice.setContent(content);
        notice.setRemark("评论了");
        notice.setNoticeType(NoticeType.COMMENT_ADD.getCode());
        notice.setReceiveFlag(ReceiveFlag.WAIT.getCode());
        notice.setCreateTime(LocalDateTime.now());
        // notice消息转换为json


        String msg = objectMapper.writeValueAsString(notice);

        rabbitTemplate.convertAndSend(NOTICE_DIRECT_EXCHANGE, NOTICE_CREATE_ROUTING_KEY, msg);
        log.debug(" ==> {} 发送了一条消息 ==> {}", NOTICE_DIRECT_EXCHANGE, msg);
    }

    /**
     * 回复评论
     */
    @PostMapping("/replay")
    public R<?> replay(@RequestBody VideoUserComment videoUserComment) {
        return R.ok(this.videoUserCommentService.replay(videoUserComment));
    }

    /**
     * 删除数据
     */
    @DeleteMapping("{commentId}")
    public R<?> removeById(@PathVariable Long commentId) {
        return R.ok(this.videoUserCommentService.delCommentByUser(commentId));
    }

    /**
     * 获取视频评论数
     */
    @GetMapping("/{videoId}")
    public R<Long> getCommentCountByVideoId(@PathVariable("videoId") String videoId) {
        return R.ok(videoUserCommentService.queryCommentCountByVideoId(videoId));
    }

    /**
     * 点赞评论接口
     */
    @GetMapping("/like/{commentId}")
    public R<Boolean> likeComment(@PathVariable("commentId") Long commentId) {

        return R.ok(true);
    }

}

