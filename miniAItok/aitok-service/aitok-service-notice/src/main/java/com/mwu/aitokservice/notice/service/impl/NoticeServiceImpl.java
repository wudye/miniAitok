package com.mwu.aitokservice.notice.service.impl;

import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitok.*;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.notice.domain.Notice;
import com.mwu.aitok.model.notice.dto.NoticePageDTO;
import com.mwu.aitok.model.notice.enums.ReceiveFlag;
import com.mwu.aitok.model.notice.vo.NoticeVO;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitokservice.notice.controller.v1.GetUserId;
import com.mwu.aitokservice.notice.repository.NoticeRepository;
import com.mwu.aitokservice.notice.service.INoticeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 通知表(Notice)表服务实现类
 *
 * @author mwu
 * @since 2023-11-08 16:21:45
 */
@Slf4j
@Service("noticeService")
public class NoticeServiceImpl  implements INoticeService {
    @Resource
    private NoticeRepository noticeMapper;

    @Resource
    private RedisService redisService;

    @GrpcClient("aitok-video")
    private VideoServiceGrpc.VideoServiceBlockingStub videoServiceBlockingStub;

    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceBlockingStub;

    /**
     * 分页未读消息
     *
     * @param pageDTO
     * @return
     */
    @Override
    public Page<Notice> queryUserNoticePage(NoticePageDTO pageDTO) {
      Long userId = GetUserId.getUserId();


        Pageable pageable = PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize(),
                Sort.by(Sort.Order.desc("createTime")));
        Page<Notice> page = noticeMapper
                .findAllByNoticeUserIdAndNoticeTypeAndReceiveFlag(userId, pageDTO.getNoticeType(), pageDTO.getReceiveFlag(), pageable);
        return page;


    }

    /**
     * 获取未读消息数量
     */
    @Override
    public Long getUnreadNoticeCount() {

        Long userId = GetUserId.getUserId();


        return noticeMapper.countByNoticeUserIdAndReceiveFlag(userId, ReceiveFlag.WAIT.getCode());
    }

    /**
     * 分页行为通知
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData getBehaveNoticePage(NoticePageDTO pageDTO) {
        Page<Notice> noticeIPage = this.queryUserBehaveNoticePage(pageDTO);
        List<Notice> records = noticeIPage.getContent();
        if (records.isEmpty()) {
            return PageData.emptyPage();
        }

        // 封装消息通知vo
        List<NoticeVO> noticeVOList = BeanCopyUtils.copyBeanList(records, NoticeVO.class);

        noticeVOList.forEach(v -> {
            // 获取操作用户信息

            GetByIdRequest request = GetByIdRequest.newBuilder().setUserId(v.getOperateUserId()).build();
            MemberResponse memberResponse = memberServiceBlockingStub.getById(request);

            Member member = BeanCopyUtils.copyBean(memberResponse, Member.class);

            if (!Objects.isNull(member)) {
                v.setNickName(member.getNickName());
                v.setOperateAvatar(member.getAvatar());
            }

            // 获取视频封面
            if (!Objects.isNull(v.getVideoId())) {
                VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder().setVideoId(Long.parseLong(v.getVideoId())).build();
                VideoResponse videoResponse = videoServiceBlockingStub.apiGetVideoByVideoId(videoIdRequest);
                Video video = BeanCopyUtils.copyBean(videoResponse, Video.class);
                v.setVideoCoverImage(video.getCoverImage());
            }

        });
        return PageData.genPageData(noticeVOList, noticeIPage.getTotalElements());
    }

    /**
     * 新增消息
     *
     * @param notice
     * @return
     */
    @Override
    public boolean saveNotice(Notice notice) {

         noticeMapper.save(notice);
        return true;
    }

    /**
     * 分页消息
     *
     * @param pageDTO
     * @return
     */
    public Page<Notice> queryUserBehaveNoticePage(NoticePageDTO pageDTO) {


        Pageable pageable = PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize(),
                Sort.by(Sort.Order.desc("createTime")));
        Long userId = GetUserId.getUserId();

        Page<Notice> page = noticeMapper.findAllByNoticeUserIdAndNoticeType(userId, pageDTO.getNoticeType(), pageable);
        return page;
    }
}
