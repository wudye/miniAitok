package com.mwu.atiokservice.behave.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.GetByIdRequest;
import com.mwu.aitok.MemberResponse;
import com.mwu.aitok.MemberServiceGrpc;
import com.mwu.aitok.VideoIdRequest;
import com.mwu.aitok.model.behave.domain.VideoUserComment;
import com.mwu.aitok.model.behave.dto.VideoCommentReplayPageDTO;
import com.mwu.aitok.model.behave.dto.VideoUserCommentPageDTO;
import com.mwu.aitok.model.behave.enums.UserVideoBehaveEnum;
import com.mwu.aitok.model.behave.vo.VideoUserCommentVO;
import com.mwu.aitok.model.behave.vo.app.AppVideoUserCommentParentVO;
import com.mwu.aitok.model.behave.vo.app.VideoCommentReplayVO;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.notice.domain.Notice;
import com.mwu.aitok.model.notice.enums.NoticeType;
import com.mwu.aitok.model.notice.enums.ReceiveFlag;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitolk.feign.member.RemoteMemberService;
import com.mwu.aitolk.feign.video.RemoteVideoService;
import com.mwu.atiokservice.behave.enums.VideoCommentStatus;
import com.mwu.atiokservice.behave.repository.VideoUserCommentRepository;
import com.mwu.atiokservice.behave.repository.VideoUserLikeRepository;
import com.mwu.atiokservice.behave.service.IUserVideoBehaveService;
import com.mwu.atiokservice.behave.service.IVideoUserCommentService;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_CREATE_ROUTING_KEY;
import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_DIRECT_EXCHANGE;


/**
 * (VideoUserComment)表服务实现类
 *
 * @author mwu
 * @since 2023-10-30 16:52:53
 */
@Slf4j
@Service("videoUserCommentService")
public class VideoUserCommentServiceImpl implements IVideoUserCommentService {
    @Resource
    private VideoUserCommentRepository videoUserCommentMapper;

    @Resource
    private VideoUserLikeRepository videoUserLikeMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RemoteMemberService remoteMemberService;

    @Resource
    private RedisService redisService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    @Resource
    private IUserVideoBehaveService userVideoBehaveService;


    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceGrpc;
    private static final long TIMEOUT_MS = 2000L;

    /**
     * 回复评论
     *
     * @param videoUserComment
     * @return
     */
    @Override
    public boolean replay(VideoUserComment videoUserComment) {
        videoUserComment.setCreateTime(LocalDateTime.now());
        // 前端需要携带parentId
        videoUserComment.setParentId(videoUserComment.getParentId());
        videoUserComment.setOriginId(videoUserComment.getOriginId());
        videoUserComment.setUserId(UserContext.getUser().getUserId());
        sendNotice2MQ(videoUserComment.getVideoId(), videoUserComment.getContent(), UserContext.getUser().getUserId());
         videoUserCommentMapper.save(videoUserComment);
         return  true;
    }

    /**
     * 用户评回复评论，通知mq
     *
     * @param videoId
     * @param operateUserId
     */
    private void sendNotice2MQ(String videoId, String content, Long operateUserId) {
        // 根据视频获取发布者id
        Video video = new Video();// videoUserLikeMapper.findByVideoId(videoId);
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
        notice.setRemark("回复了你的评论");
        notice.setNoticeType(NoticeType.COMMENT_ADD.getCode());
        notice.setReceiveFlag(ReceiveFlag.WAIT.getCode());
        notice.setCreateTime(LocalDateTime.now());
        // notice消息转换为json
        String msg;
        try {
            msg = OBJECT_MAPPER.writeValueAsString(notice);
        } catch (JsonProcessingException e) {
            log.error("serialize notice failed", e);
            return;
        }
        rabbitTemplate.convertAndSend(NOTICE_DIRECT_EXCHANGE, NOTICE_CREATE_ROUTING_KEY, msg);
        log.debug(" ==> {} 发送了一条消息 ==> {}", NOTICE_DIRECT_EXCHANGE, msg);
    }

    /**
     * 用户删除自己的评论
     *
     * @param commentId
     * @return
     */
    @Override
    public boolean delCommentByUser(Long commentId) {
        Long userId = UserContext.getUser().getUserId();

        // 隐式删除
        VideoUserComment videoUserComment = videoUserCommentMapper.findByUserIdAndCommentId(userId, commentId);
        if (videoUserComment == null) {
            return true;
        }
        videoUserComment.setStatus(VideoCommentStatus.DELETED.getCode());
        videoUserCommentMapper.save(videoUserComment);

        // 异步删除子评论
        deleteOriginChildren(commentId);
        return true;
    }

    /**
     * 批量删除祖先评论下的所有子评论
     */
    @Async
    public void deleteOriginChildren(Long commentId) {
        // 先查出此评论
        VideoUserComment byId = videoUserCommentMapper.findByCommentId(commentId);
        if (byId == null) {
            return;
        }
        if (byId.getParentId() == 0 && byId.getOriginId() == 0) {

            videoUserCommentMapper.deleteByCommentId(commentId);
        }
    }

    /**
     * 分页根据视频id获取评论根id
     *
     * @param pageDTO
     * @return
     */
    @Override
    public Page<VideoUserComment> getRootListByVideoId(VideoUserCommentPageDTO pageDTO) {
        int page = Math.max(0, pageDTO.getPageNum() - 1);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page,
                pageDTO.getPageSize(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createTime")
        );
        return videoUserCommentMapper.findAllByVideoIdAndParentIdAndStatus(
                pageDTO.getVideoId(),
                0,
                VideoCommentStatus.NORMAL.getCode(),
                pageable
        );
    }

    /**
     * 获取子评论
     *
     * @param commentId
     * @return
     */
    @Override
    public List<VideoUserComment> getChildren(Long commentId) {
        return videoUserCommentMapper.findByOriginIdAndStatus(commentId, VideoCommentStatus.NORMAL.getCode());

    }

    /**
     * 查找指定视频评论量 对逻辑删除过的评论进行过滤
     *
     * @param videoId
     * @return
     */
    @Override
    public Long queryCommentCountByVideoId(String videoId) {
        return videoUserCommentMapper.countByVideoIdAndStatus(videoId, VideoCommentStatus.NORMAL.getCode());
    }

    /**
     * 分页查询评论树
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData getCommentPageTree(VideoUserCommentPageDTO pageDTO) {
        String videoId = pageDTO.getVideoId();
        if (StringUtil.isEmpty(videoId)) {
            return PageData.emptyPage();
        }
        Pageable pageable = PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize(), Sort.by(Sort.Direction.DESC, pageDTO.getOrderBy()));

        Page<VideoUserComment> ipage = videoUserCommentMapper.findAllByVideoIdAndParentIdAndStatus(videoId, 0, VideoCommentStatus.NORMAL.getCode(), pageable);

        List<VideoUserComment> rootRecords = ipage.getContent();

        List<VideoUserCommentVO> voList = new ArrayList<>();
        CompletableFuture.allOf(rootRecords.stream()
                .map(r -> CompletableFuture.runAsync(() -> {
                    // 获取用户详情
                    VideoUserCommentVO appNewsCommentVO = BeanCopyUtils.copyBean(r, VideoUserCommentVO.class);
                    Long userId = r.getUserId();
                    GetByIdRequest getByIdRequest = GetByIdRequest.newBuilder().setUserId(userId).build();
                    MemberResponse memberResponse = memberServiceGrpc.getById(getByIdRequest);
                    if (StringUtils.isNotNull(memberResponse)) {
                        appNewsCommentVO.setNickName(StringUtils.isEmpty(memberResponse.getNickName()) ? "-" : memberResponse.getNickName());
                        appNewsCommentVO.setAvatar(StringUtils.isEmpty(memberResponse.getAvatar()) ? "" : memberResponse.getAvatar());
                    }
                    Long commentId = r.getCommentId();
                    List<VideoUserComment> children = this.getChildren(commentId);
                    List<VideoUserCommentVO> childrenVOS = BeanCopyUtils.copyBeanList(children, VideoUserCommentVO.class);
                    CompletableFuture.allOf(childrenVOS.stream()
                            .map(c -> CompletableFuture.runAsync(() -> {
                                Long userId11 = c.getUserId();
                                GetByIdRequest getByIdRequest1 = GetByIdRequest.newBuilder().setUserId(userId11).build();
                                MemberResponse memberResponse1 = memberServiceGrpc.getById(getByIdRequest1);
                                if (StringUtils.isNotNull(memberResponse1)) {
                                    c.setNickName(StringUtils.isEmpty(memberResponse1.getNickName()) ? "-" : memberResponse1.getNickName());
                                    c.setAvatar(StringUtils.isEmpty(memberResponse1.getAvatar()) ? "" : memberResponse1.getAvatar());
                                }
                                if (!c.getParentId().equals(commentId)) {
                                    // 回复了回复
                                    VideoUserComment byId = videoUserCommentMapper.findById(c.getParentId()).orElse(null);
                                    if (byId == null) {
                                        return;
                                    }
                                    Long userId1 = byId.getUserId();
                                    c.setReplayUserId(byId.getUserId());
                                    GetByIdRequest getByIdRequest2 = GetByIdRequest.newBuilder().setUserId(userId1).build();
                                    MemberResponse memberResponse2 = memberServiceGrpc.getById(getByIdRequest2);
                                    if (StringUtils.isNotNull(memberResponse2)) {
                                        c.setReplayUserNickName(memberResponse2.getNickName());
                                    }
                                }
                            })).toArray(CompletableFuture[]::new)).join();
                    appNewsCommentVO.setChildren(childrenVOS);
                    voList.add(appNewsCommentVO);
                })).toArray(CompletableFuture[]::new)).join();
        // 获取总评论数
        Long queryCommentCountByVideoId = this.queryCommentCountByVideoId(videoId);
        return PageData.genPageData(voList, queryCommentCountByVideoId);
    }

    /**
     * 删除视频所有评论
     *
     * @param videoId
     * @return
     */
    @Override
    public boolean removeCommentByVideoId(String videoId) {
        videoUserCommentMapper.deleteByVideoId(videoId);
        return true;
    }

    /**
     * 分页视频父评论
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData getCommentParentPage(VideoUserCommentPageDTO pageDTO) {
        pageDTO.setPageNum((pageDTO.getPageNum() - 1) * pageDTO.getPageSize());
        switch (pageDTO.getOrderBy()) {
            case "0":
                pageDTO.setOrderBy("create_time");
                break;
            case "1":
                pageDTO.setOrderBy("like_num");
                break;
            default:
                pageDTO.setOrderBy("create_time");
                break;
        }
        Pageable pageable = PageRequest.of(pageDTO.getPageNum(), pageDTO.getPageSize(), Sort.by(Sort.Direction.DESC, pageDTO.getOrderBy()));
        Page<VideoUserComment> page = videoUserCommentMapper.findAllByOriginIdAndStatus(0L, VideoCommentStatus.NORMAL.getCode(), pageable);
        List<VideoUserComment> videoUserComments = page.getContent();
        // 封装父评论
        List<AppVideoUserCommentParentVO> appVideoUserCommentParentVOS = BeanCopyUtils.copyBeanList(videoUserComments, AppVideoUserCommentParentVO.class);


        // 获取总评论数
        Long queryCommentCountByVideoId = this.queryCommentCountByVideoId(pageDTO.getVideoId());
        return PageData.genPageData(appVideoUserCommentParentVOS, queryCommentCountByVideoId);
    }

    /**
     * 评论视频
     *
     * @param videoUserComment
     * @return
     */
    @Override
    public boolean commentVideo(VideoUserComment videoUserComment) {
        videoUserComment.setCreateTime(LocalDateTime.now());
        videoUserComment.setUserId(UserContext.getUser().getUserId());
        videoUserCommentMapper.save(videoUserComment);

            commentVideoSendNotice2MQ(videoUserComment.getVideoId(), videoUserComment.getContent(), UserContext.getUser().getUserId());
            // 插入收藏行为数据
            userVideoBehaveService.syncUserVideoBehave(UserContext.getUser().getUserId(), videoUserComment.getVideoId(), UserVideoBehaveEnum.COMMENT);

        return true;
    }

    /**
     * 用户评论视频，通知mq
     *
     * @param videoId
     * @param operateUserId
     */
    private void commentVideoSendNotice2MQ(String videoId, String content, Long operateUserId) {
        // 根据视频获取发布者id
        Video video = new Video();// videoUserLikeMapper.findByVideoId(videoId);
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
        String msg;
        try {
            msg = OBJECT_MAPPER.writeValueAsString(notice);
        } catch (JsonProcessingException e) {
            log.error("serialize notice failed", e);
            return;
        }
        rabbitTemplate.convertAndSend(NOTICE_DIRECT_EXCHANGE, NOTICE_CREATE_ROUTING_KEY, msg);
        log.debug(" ==> {} 发送了一条消息 ==> {}", NOTICE_DIRECT_EXCHANGE, msg);
    }

    /**
     * 视频评论回复分页
     */
    @Override
    public PageData getCommentReplyPage(VideoCommentReplayPageDTO pageDTO) {
        pageDTO.setPageNum((pageDTO.getPageNum() - 1) * pageDTO.getPageSize());
        switch (pageDTO.getOrderBy()) {
            case "0":
                pageDTO.setOrderBy("create_time");
                break;
            case "1":
                pageDTO.setOrderBy("like_num");
                break;
            default:
                pageDTO.setOrderBy("create_time");
                break;
        }
        Pageable pageable = PageRequest.of(pageDTO.getPageNum(), pageDTO.getPageSize(), Sort.by(Sort.Direction.DESC, pageDTO.getOrderBy()));
        Long commentId = pageDTO.getCommentId();

        Page<VideoUserComment> page = videoUserCommentMapper.findAllByOriginIdAndStatus(commentId, VideoCommentStatus.NORMAL.getCode(), pageable);


        List<VideoUserComment> videoUserComments = page.getContent();
        Long total = videoUserCommentMapper.countVideoUserCommentByOriginIdAndStatus(commentId, VideoCommentStatus.NORMAL.getCode());
        // 封装回复
        List<VideoCommentReplayVO> videoCommentReplayVOS = BeanCopyUtils.copyBeanList(videoUserComments, VideoCommentReplayVO.class);
        videoCommentReplayVOS.forEach(c -> {

            Long userId = c.getUserId();
            GetByIdRequest getByIdRequest = GetByIdRequest.newBuilder().setUserId(userId).build();
            MemberResponse memberResponse = memberServiceGrpc.getById(getByIdRequest);
            Member member = new Member();


            if (StringUtils.isNotNull(memberResponse)) {
                member.setNickName(memberResponse.getNickName());
                member.setAvatar(memberResponse.getAvatar());
                member.setUserId(memberResponse.getUserId());

            }



            // 回复
            if (!c.getParentId().equals(c.getOriginId())) {
                // 回复了回复
                VideoUserComment byReplayComment = videoUserCommentMapper.findByParentId(c.getParentId());
                Long byReplayUserId = byReplayComment.getUserId();
                c.setReplayUserId(byReplayComment.getUserId());
                 getByIdRequest = GetByIdRequest.newBuilder().setUserId(byReplayUserId).build();

                 memberResponse = memberServiceGrpc.getById(getByIdRequest);
                if (StringUtils.isNotNull(memberResponse)) {
                    member.setNickName(memberResponse.getNickName());
                    member.setAvatar(memberResponse.getAvatar());
                    member.setUserId(memberResponse.getUserId());
                }

            }
        });
        return PageData.genPageData(videoCommentReplayVOS, total);
    }

    /**
     * 获取用户评论视频记录
     *
     * @param userId
     * @return
     */
    @Override
    public List<String> getUserCommentVideoIdsRecord(Long userId) {
        return videoUserCommentMapper.getVideoUserCommentByUserId(userId);
    }
}
