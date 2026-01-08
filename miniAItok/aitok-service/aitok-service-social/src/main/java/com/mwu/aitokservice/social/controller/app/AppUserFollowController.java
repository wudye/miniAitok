package com.mwu.aitokservice.social.controller.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitok.model.social.cache.DynamicUser;
import com.mwu.aitok.model.social.vo.Fans;
import com.mwu.aitok.model.social.vo.FollowUser;
import com.mwu.aitok.model.video.vo.VideoVO;
import com.mwu.aitokservice.social.service.IUserFollowService;
import com.mwu.aitokservice.social.service.SocialDynamicsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * AppUserFollowController
 *
 * @AUTHOR: mwu
 * @DATE: 2024/2/1
 **/
@RestController
@RequestMapping("/api/v1/app/follow")
public class AppUserFollowController {

    @Resource
    private IUserFollowService userFollowService;

    @Resource
    private SocialDynamicsService socialDynamicsService;

    /**
     * 推送关注的人视频 拉模式
     *
     * @param lastTime 滚动分页
     */
    @GetMapping("/videoFeed")
    public R<?> appFollowFeed(@RequestParam(required = false) Long lastTime) {
        return R.ok(userFollowService.followVideoFeed(lastTime));
    }

    /**
     * 关注动态
     */
    @GetMapping("/dynamic")
    public PageData<DynamicUser> followDynamicPage() {
        return socialDynamicsService.getSocialDynamicsUser();
    }

    /**
     * 动态视频
     */
    @PostMapping("/dynamicVideoPage")
    public PageData<VideoVO> dynamicVideoPage(@RequestBody PageDTO pageDTO) {
        return userFollowService.getSocialDynamicVideoPage(pageDTO);
    }

    /**
     * 初始化用户收件箱
     * todo 返回未读视频动态数
     */
    @GetMapping("/initUserInBox")
    public R<?> initUserInBox() {
        socialDynamicsService.initUserFollowInBox(UserContext.getUserId());
        return R.ok(true);
    }

    /**
     * 关注用户
     */
    @GetMapping("/{userId}")
    public R<Boolean> followUser(@PathVariable("userId") Long userId) throws JsonProcessingException {
        return R.ok(userFollowService.followUser(userId));
    }

    /**
     * 我的关注分页
     */
    @PostMapping("/followPage")
    public PageData<FollowUser> followPage(@RequestBody PageDTO pageDTO) {
        return userFollowService.appGetFollowPage(pageDTO);
    }

    /**
     * 我的粉丝分页
     */
    @PostMapping("/fansPage")
    public PageData<Fans> fansPage(@RequestBody PageDTO pageDTO) {
        return userFollowService.appGetFansPage(pageDTO);
    }
}
