package com.mwu.aitokservice.notice.controller.v1;


import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.*;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.notice.domain.Notice;
import com.mwu.aitok.model.notice.dto.NoticePageDTO;
import com.mwu.aitok.model.notice.vo.NoticeVO;
import com.mwu.aitok.model.notice.vo.WebSocketBaseResp;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitokservice.notice.enums.WebSocketMsgType;
import com.mwu.aitokservice.notice.repository.NoticeRepository;
import com.mwu.aitokservice.notice.service.INoticeService;
import jakarta.annotation.Resource;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 通知表(Notice)表控制层
 *
 * @author mwu
 * @since 2023-11-08 16:21:43
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class NoticeController {

    @Resource
    private INoticeService noticeService;

    @Resource
    private RedisService redisService;

    @Resource
    NoticeRepository noticeMapper;




    /**
     * 未读消息数量
     */
    @GetMapping("/app/unreadCount")
    public R<Long> unReadNoticeCount() {
        return R.ok(noticeService.getUnreadNoticeCount());
    }

    /**
     * 分页根据条件查询行为通知
     *
     * @param pageDTO
     * @return
     */
    @PostMapping("/app/behavePage")
    public PageData behaveNoticePage(@RequestBody NoticePageDTO pageDTO) {
        return noticeService.getBehaveNoticePage(pageDTO);
    }



    @GrpcClient("aitok-video")
    private VideoServiceGrpc.VideoServiceBlockingStub videoServiceBlockingStub;

    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceBlockingStub;
    /**
     * 分页根据条件查询
     *
     * @param pageDTO
     * @return
     */
    @PostMapping("/page")
    public PageData userNoticePage(@RequestBody NoticePageDTO pageDTO) {
        Page<Notice> noticeIPage = noticeService.queryUserNoticePage(pageDTO);
        List<Notice> records = noticeIPage.getContent();
        if (records.isEmpty()) {
            return PageData.emptyPage();
        }
//        List<NoticeVO> voList = new ArrayList<>(10);
//        //封装vo
//        records.forEach(n -> {
//            NoticeVO noticeVO = BeanCopyUtils.copyBean(n, NoticeVO.class);
//            // 先走redis，有就直接返回
//            Member userCache = redisService.getCacheObject("member:userinfo:" + n.getOperateUserId());
//            if (StringUtils.isNotNull(userCache)) {
//                noticeVO.setNickName(userCache.getNickName());
//                noticeVO.setOperateAvatar(userCache.getAvatar());
//            } else {
//                Member user = new Member();
//                List<Member> members = noticeMapper.batchSelectVideoAuthor(Collections.singletonList(n.getOperateUserId()));
//                if (!members.isEmpty()) {
//                    user = members.get(0);
//                }
//                if (StringUtils.isNotNull(user)) {
//                    noticeVO.setNickName(user.getNickName());
//                    noticeVO.setOperateAvatar(user.getAvatar());
//                }
//            }
//            // 封装视频封面
//            if (StringUtils.isNotNull(n.getVideoId())) {
//                Video videoCache = redisService.getCacheObject("video:videoinfo:" + n.getVideoId());
//                if (StringUtils.isNull(videoCache)) {
//                    //缓存为空
//                    Video video = noticeMapper.selectVideoById(n.getVideoId());
//                    noticeVO.setVideoCoverImage(video.getCoverImage());
//                } else {
//                    noticeVO.setVideoCoverImage(videoCache.getCoverImage());
//                }
//            }
//            voList.add(noticeVO);
//        });
        List<CompletableFuture<NoticeVO>> futures = records.stream()
                .map(n -> CompletableFuture.supplyAsync(() -> {
                    NoticeVO noticeVO = BeanCopyUtils.copyBean(n, NoticeVO.class);
                    Member user = new Member();
                    // 获取用户信息
                    Member userCache = redisService.getCacheObject("member:userinfo:" + n.getOperateUserId());
                    if (StringUtils.isNotNull(userCache)) {
                        noticeVO.setNickName(userCache.getNickName());
                        noticeVO.setOperateAvatar(userCache.getAvatar());
                    } else {
                        /*
                        GetByIdRequest memberRequest = GetByIdRequest.newBuilder().setUserId(n.getOperateUserId()).build();
                        MemberResponse memberResponse = memberServiceBlockingStub.getById(memberRequest);
                        Member member = BeanCopyUtils.copyBean(memberResponse, Member.class);
                        */

                        List<Long> ids = Collections.singletonList(n.getOperateUserId()); // 不可变，大小为1

                        GetInIdsRequest memberRequest = GetInIdsRequest.newBuilder().addAllUserIds(ids).build();
                        MemberListResponse memberResponse = memberServiceBlockingStub.getInIds(memberRequest);


                        List<Member> members = memberResponse.getMembersList().stream().map(
                                m -> BeanCopyUtils.copyBean(m, Member.class)
                        ).toList();
                        if (!members.isEmpty()) {
                            user = members.get(0);
                        }
                        if (StringUtils.isNotNull(user)) {
                            noticeVO.setNickName(user.getNickName());
                            noticeVO.setOperateAvatar(user.getAvatar());
                        }
                    }
                    // 获取视频封面
                    if (StringUtils.isNotNull(n.getVideoId())) {
                        Video videoCache = redisService.getCacheObject("video:videoinfo:" + n.getVideoId());
                        if (StringUtils.isNull(videoCache)) {
                            // 缓存为空
                            VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder().setVideoId(Long.parseLong(n.getVideoId())).build();
                            VideoResponse videoResponse = videoServiceBlockingStub.apiGetVideoByVideoId(videoIdRequest);
                            Video video = BeanCopyUtils.copyBean(videoResponse, Video.class);
                            noticeVO.setVideoCoverImage(video.getCoverImage());
                        } else {
                            noticeVO.setVideoCoverImage(videoCache.getCoverImage());
                        }
                    }
                    return noticeVO;
                }))
                .collect(Collectors.toList());
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        List<NoticeVO> voList = allFutures.thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()))
                .join();
        return PageData.genPageData(voList, noticeIPage.getTotalElements());
    }

    /**
     * 删除通知
     *
     * @param noticeId
     * @return
     */
    @DeleteMapping("/delete")
    public R<String> delNotice(@RequestParam("noticeId") String noticeId) {

        Long n = Long.valueOf(noticeId);
        noticeMapper.deleteByNoticeId(n);
        return R.ok("finish delete");
    }

    /**
     * 未读消息数量
     *
     * @return
     */
    @PostMapping("/count")
    public R<Long> noticeCount(@RequestBody Notice notice) {

        Long res = noticeMapper.countByNoticeIdAndReceiveFlag(notice.getNoticeId(), notice.getReceiveFlag());
        return R.ok(res);
    }


    @GetMapping("/ws/push")
    public void pushNotice(@PathParam("msg") String msg) {
        /*

        WebSocketBaseResp<String> 的作用是标准化WebSocket消息格式，而不是直接发送简单的字符串。
        {
          "code": 1001,
          "message": "success",
          "data": "hello",
          "timestamp": 1642345678901
        }

         */
        WebSocketBaseResp<String> res = WebSocketBaseResp.build(WebSocketMsgType.NOTICE_UNREAD_COUNT.getCode(), msg);
        WebSocketServer.sendOneMessage(GetUserId.getUserId(), res);
    }

}

