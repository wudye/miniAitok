package com.mwu.aitokservice.social.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitok.model.social.domain.UserFollow;
import com.mwu.aitok.model.social.vo.Fans;
import com.mwu.aitok.model.social.vo.FollowUser;
import com.mwu.aitok.model.video.vo.VideoVO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 用户关注表(UserFollow)表服务接口
 *
 * @author roydon
 * @since 2023-10-30 15:54:21
 */
public interface IUserFollowService  {

    /**
     * 关注用户
     *
     * @param userId 被关注用户id
     */
    boolean followUser(Long userId) throws JsonProcessingException;

    /**
     * 取消关注
     *
     * @param userId 取消关注用户id
     */
    boolean unFollowUser(Long userId);

    /**
     * 分页查询用户关注列表
     *
     * @param pageDTO 分页对象
     * @return IPage<User>
     */
    Page<UserFollow> followPage(PageDTO pageDTO);

    /**
     * 分页查询我的关注
     *
     * @param pageDTO
     * @return
     */
    PageData getFollowPage(PageDTO pageDTO);

    /**
     * 分页用户粉丝
     *
     * @param pageDTO
     * @return
     */
    PageData queryUserFansPage(PageDTO pageDTO);

    List<UserFollow> getFollowList(Long userId);

    void initFollowVideoFeed();

    /**
     * 关注流
     *
     * @param lastTime 滚动分页参数，首次为null，后续为上次的末尾视频时间
     * @return
     */
    List<VideoVO> followVideoFeed(Long lastTime);

    /**
     * 获取社交动态分页
     *
     * @param pageDTO
     * @return
     */
    PageData<VideoVO> getSocialDynamicVideoPage(PageDTO pageDTO);

    /**
     * 是否关注用户
     *
     * @param userId
     * @param followUserId
     * @return
     */
    Boolean weatherFollow(Long userId, Long followUserId);

    Long getUserFollowCount(Long userId);

    Long getUserFansCount(Long userId);

    /**
     * 分页我的关注列表
     */
    PageData<FollowUser> appGetFollowPage(PageDTO pageDTO);

    Page<UserFollow> fansPage(PageDTO pageDTO);

    /**
     * 分页我的粉丝
     *
     * @param pageDTO
     * @return
     */
    PageData<Fans> appGetFansPage(PageDTO pageDTO);
}
