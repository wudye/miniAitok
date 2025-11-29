package com.mwu.aitokservice.social.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.date.DateUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.*;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.notice.domain.Notice;
import com.mwu.aitok.model.notice.enums.NoticeType;
import com.mwu.aitok.model.notice.enums.ReceiveFlag;
import com.mwu.aitok.model.social.domain.UserFollow;
import com.mwu.aitok.model.social.vo.Fans;
import com.mwu.aitok.model.social.vo.FollowUser;
import com.mwu.aitok.model.video.vo.VideoVO;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitokservice.social.repository.UserFollowRepository;
import com.mwu.aitokservice.social.service.IUserFollowService;
import com.mwu.aitokservice.social.service.SocialDynamicsService;
import com.mwu.aitolk.feign.member.RemoteMemberService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mwu.aitok.model.cache.SocialCacheConstants.SOCIAL_DYNAMICS;
import static com.mwu.aitok.model.constants.VideoConstants.IN_FOLLOW;
import static com.mwu.aitok.model.cache.SocialCacheConstants.FOLLOW;
import static com.mwu.aitok.model.constants.VideoConstants.OUT_FOLLOW;
import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_CREATE_ROUTING_KEY;
import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_DIRECT_EXCHANGE;


/**
 * 用户关注表(UserFollow)表服务实现类
 *
 * @author mwu
 * @since 2023-10-30 15:54:21
 */
@Slf4j
@Service("userFollowService")
public class UserFollowServiceImpl implements IUserFollowService {
    @Resource
    private UserFollowRepository userFollowMapper;

    @Resource
    private RemoteMemberService remoteMemberService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceBlockingStub;

    //     @DubboReference(mock = "return null")
    @GrpcClient("aitok-video")
    private VideoServiceGrpc.VideoServiceBlockingStub videoServiceBlockingStub;



    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedisService redisService;

    @Resource
    private SocialDynamicsService socialDynamicsService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 关注用户
     *
     * @param userId 被关注用户id
     */
    @Override
    public boolean followUser(Long userId) throws JsonProcessingException {
        Long loginUserId = UserContext.getUserId();
        if (StringUtils.isNull(userId)) {
            return false;
        }
        if (loginUserId.equals(userId)) {
            // 不可关注自己
            throw new CustomException(HttpCodeEnum.NOT_ALLOW_FOLLOW_YOURSELF);
        }
        Member user = remoteMemberService.userInfoById(userId).getData();
        if (StringUtils.isNull(user)) {
            // 用户不存在
            throw new CustomException(HttpCodeEnum.USER_NOT_EXIST);
        }

        List<UserFollow> list = userFollowMapper.findAllByUserIdAndUserFollowId(loginUserId, userId);
        if (!list.isEmpty()) {
            // 已关注
            throw new CustomException(HttpCodeEnum.ALREADY_FOLLOW);
        }
        LocalDateTime now = LocalDateTime.now();
        UserFollow userFollow = new UserFollow();
        userFollow.setUserId(loginUserId);
        userFollow.setUserFollowId(userId);
        userFollowMapper.save(userFollow);

            // 发送消息到mq
            sendNotice2MQ(loginUserId, userId);
            // 初始化用户关注视频收件箱
            List<Long> followUserIds = this.getFollowList(loginUserId).stream().map(UserFollow::getUserFollowId).toList();
            FollowVideoFeedRequest  request = FollowVideoFeedRequest.newBuilder()
                    .setUserId(loginUserId)
                    .addAllFollowIds(followUserIds)
                    .build();
            videoServiceBlockingStub.apiInitFollowVideoFeed(request);
            // 缓存如用户关注缓存
            redisService.setCacheZSet(FOLLOW + loginUserId, userId, DateUtils.toDate(now).getTime());

        return true;
    }

    /**
     * 用户关注，通知mq
     *
     * @param operateUserId
     */
    private void sendNotice2MQ(Long operateUserId, Long userId) throws JsonProcessingException {
        if (operateUserId.equals(userId)) {
            return;
        }
        // 封装notice实体
        Notice notice = new Notice();
        notice.setOperateUserId(operateUserId);
        notice.setNoticeUserId(userId);
        notice.setContent("关注了你");
        notice.setNoticeType(NoticeType.FOLLOW.getCode());
        notice.setReceiveFlag(ReceiveFlag.WAIT.getCode());
        notice.setCreateTime(LocalDateTime.now());
        // notice消息转换为json

        String msg ;
        msg = objectMapper.writeValueAsString(notice);
        rabbitTemplate.convertAndSend(NOTICE_DIRECT_EXCHANGE, NOTICE_CREATE_ROUTING_KEY, msg);
        log.debug(" ==> {} 发送了一条消息 ==> {}", NOTICE_DIRECT_EXCHANGE, msg);
    }

    /**
     * 取消关注
     *
     * @param userId 取消关注用户id
     */
    @Override
    public boolean unFollowUser(Long userId) {
        Long loginUserId = UserContext.getUser().getUserId();
        if (StringUtils.isNull(userId) || StringUtils.isNull(loginUserId)) {
            return false;
        }

        userFollowMapper.deleteByUserIdAndUserFollowId(loginUserId, userId);
        return true;
    }

    /**
     * 分页查询用户关注列表
     *
     * @param pageDTO 分页对象
     * @return IPage<User>
     */
    @Override
    public Page<UserFollow> followPage(PageDTO pageDTO) {


        Pageable pageable = PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize(),
                Sort.Direction.DESC, "createTime");

        Page<UserFollow> userFollowPage = userFollowMapper.findAllByUserId(UserContext.getUserId(), pageable);
        return userFollowPage;
    }

    /**
     * 分页查询我的关注
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData getFollowPage(PageDTO pageDTO) {
        if (StringUtils.isNull(pageDTO)) {

            List<UserFollow> list = userFollowMapper.findAllByUserId(UserContext.getUserId());
            List<Member> res = new ArrayList<>();
            CompletableFuture.allOf(list.stream()
                    .map(l -> CompletableFuture.runAsync(() -> {
                        GetByIdRequest request = GetByIdRequest.newBuilder().setUserId(l.getUserFollowId()).build();
                        MemberResponse memberResponse = memberServiceBlockingStub.getById(request);
                        Member user = BeanCopyUtils.copyBean(memberResponse, Member.class);

                        res.add(user);
                    })).toArray(CompletableFuture[]::new)).join();
            return PageData.genPageData(res, res.size());
        }
        Page<UserFollow> userFollowIPage = this.followPage(pageDTO);
        List<Member> userList = new ArrayList<>();
        userFollowIPage.getContent().forEach(uf -> {
            GetByIdRequest request = GetByIdRequest.newBuilder().setUserId(uf.getUserFollowId()).build();
            MemberResponse memberResponse = memberServiceBlockingStub.getById(request);
            Member user = BeanCopyUtils.copyBean(memberResponse, Member.class);
            userList.add(user);
        });
        return PageData.genPageData(userList, userFollowIPage.getTotalElements());
    }

    /**
     * 分页用户粉丝
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData queryUserFansPage(PageDTO pageDTO) {
        Long userId = UserContext.getUserId();

        Pageable pageable = PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize(),
                Sort.Direction.DESC, "createTime");
        Page<UserFollow> page = userFollowMapper.findAllByUserId(userId, pageable);
        List<UserFollow> records = page.getContent();
        if (records.isEmpty()) {
            return PageData.emptyPage();
        }
        List<Long> fansUserIds = records.stream().map(UserFollow::getUserId).collect(Collectors.toList());
        GetInIdsRequest request = GetInIdsRequest.newBuilder().addAllUserIds(fansUserIds).build();
        MemberListResponse memberResponse = memberServiceBlockingStub.getInIds(request);
        List<Member> memberList = memberResponse.getMembersList().stream().map(m -> BeanCopyUtils.copyBean(m, Member.class)).collect(Collectors.toList());


        return PageData.genPageData(memberList, page.getTotalElements());
    }

    @Override
    public List<UserFollow> getFollowList(Long userId) {

        return userFollowMapper.findAllByUserId(userId);
    }

    @Override
    public void initFollowVideoFeed() {
        Long loginUserId = UserContext.getUserId();
        FollowVideoFeedRequest request = FollowVideoFeedRequest.newBuilder()
                .setUserId(loginUserId)
                .addAllFollowIds(this.getFollowList(loginUserId).stream().map(UserFollow::getUserFollowId).toList())
                .build();
        // 初始化用户关注视频收件箱
        videoServiceBlockingStub.apiInitFollowVideoFeed(request);
    }

    /**
     * 关注流
     *
     * @param lastTime 滚动分页参数，首次为null，后续为上次的末尾视频时间
     * @return
     */
    @Override
    public List<VideoVO> followVideoFeed(Long lastTime) {
        Long userId = UserContext.getUserId();
        // 是否存在
        Set<Object> videoIds = redisTemplate.opsForZSet().reverseRangeByScore(IN_FOLLOW + userId,
                0,
                lastTime == null ? new Date().getTime() : lastTime,
                lastTime == null ? 0 : 1,
                10);
        if (ObjectUtils.isEmpty(videoIds)) {
            return new ArrayList<>();
        }
        List<String> collect = videoIds.stream().map(Object::toString).collect(Collectors.toList());

        VideoVORequest request = VideoVORequest.newBuilder()
                .setLoginUserId(userId)
                .addAllVideoIds(collect)
                .build();
        VideoVOListResponse response = videoServiceBlockingStub.apiGetVideoVOListByVideoIds(request);
        List<VideoVO> videoVOList = response.getVideoVoListList().stream()
                .map(v -> BeanCopyUtils.copyBean(v, VideoVO.class))
                .toList();
        return videoVOList;
    }

    /**
     * 获取社交动态分页
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData<VideoVO> getSocialDynamicVideoPage(PageDTO pageDTO) {
        PageData<String> socialDynamics = socialDynamicsService.getSocialDynamics(pageDTO);
        // 封装视频vo
        List<String> videoIds = socialDynamics.getRows();
        if (videoIds.isEmpty()) {
            return PageData.emptyPage();
        }
        VideoVORequest request = VideoVORequest.newBuilder()
                .setLoginUserId(UserContext.getUserId())
                .addAllVideoIds(videoIds)
                .build();
        VideoVOListResponse response = videoServiceBlockingStub.apiGetVideoVOListByVideoIds(request);
        List<VideoVO> videoVOList = response.getVideoVoListList().stream()
                .map(v -> BeanCopyUtils.copyBean(v, VideoVO.class))
                .toList();

        return PageData.genPageData(videoVOList, socialDynamics.getTotal());
    }

    /**
     * 是否关注用户
     *
     * @param userId
     * @param followUserId
     * @return
     */
    @Override
    public Boolean weatherFollow(Long userId, Long followUserId) {

        Integer count = userFollowMapper.countByUserIdAndUserFollowId(userId, followUserId);
        return count > 0;
    }

    @Override
    public Long getUserFollowCount(Long userId) {


        return userFollowMapper.countByUserId(userId);
    }

    @Override
    public Long getUserFansCount(Long userId) {

        return userFollowMapper.countByUserId(userId);
    }

    /**
     * 分页我的关注列表
     *
     * @param pageDTO
     */
    @Override
    public PageData<FollowUser> appGetFollowPage(PageDTO pageDTO) {
        Page<UserFollow> userFollowIPage = this.followPage(pageDTO);
        List<FollowUser> followUserList = new ArrayList<>();
        userFollowIPage.getContent().forEach(uf -> {

            GetByIdRequest request = GetByIdRequest.newBuilder().setUserId(uf.getUserFollowId()).build();
            MemberResponse memberResponse = memberServiceBlockingStub.getById(request);
            Member user = BeanCopyUtils.copyBean(memberResponse, Member.class);
            followUserList.add(BeanCopyUtils.copyBean(user, FollowUser.class));
        });
        return PageData.genPageData(followUserList, userFollowIPage.getTotalElements());
    }

    @Override
    public Page<UserFollow> fansPage(PageDTO pageDTO) {

        Pageable pageable = PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize(),
                Sort.Direction.DESC, "createTime");
        Page<UserFollow> userFollowPage = userFollowMapper.findAllByUserId(UserContext.getUserId(), pageable);

        return userFollowPage;
    }

    /**
     * 分页我的粉丝
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData<Fans> appGetFansPage(PageDTO pageDTO) {
        Page<UserFollow> userFollowIPage = this.fansPage(pageDTO);
        List<Fans> fansList = new ArrayList<>();
        userFollowIPage.getContent().forEach(uf -> {
            GetByIdRequest request = GetByIdRequest.newBuilder().setUserId(uf.getUserFollowId()).build();
            MemberResponse memberResponse = memberServiceBlockingStub.getById(request);
            Member user = BeanCopyUtils.copyBean(memberResponse, Member.class);
            Fans fans = BeanCopyUtils.copyBean(user, Fans.class);
            fans.setWeatherFollow(weatherFollow(UserContext.getUserId(), uf.getUserId()));
            fansList.add(fans);
        });
        return PageData.genPageData(fansList, userFollowIPage.getTotalElements());
    }
}
