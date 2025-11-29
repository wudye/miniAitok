package com.mwu.aitokservice.social.controller.v1;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.behave.vo.UserFollowsFansVo;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitok.model.social.domain.UserFollow;
import com.mwu.aitokservice.social.repository.UserFollowRepository;
import com.mwu.aitokservice.social.service.IUserFollowService;
import com.mwu.aitokservice.social.service.SocialDynamicsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关注表(UserFollow)表控制层
 *
 * @author mwu
 * @since 2023-10-30 15:54:19
 */
@RestController
@RequestMapping("/api/v1/follow")
public class UserFollowController {

    @Resource
    private IUserFollowService userFollowService;

    @Resource
    private SocialDynamicsService socialDynamicsService;

    @Resource
    private UserFollowRepository userFollowRepository;

    /**
     * 关注
     */
    @GetMapping("/{userId}")
    public R<?> follow(@PathVariable("userId") Long userId) throws JsonProcessingException {
        return R.ok(userFollowService.followUser(userId));
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/{userId}")
    public R<?> unfollow(@PathVariable("userId") Long userId) {
        return R.ok(userFollowService.unFollowUser(userId));
    }

    /**
     * 分页查询我的关注列表
     */
    @PostMapping("/page")
    public PageData followPage(@RequestBody PageDTO pageDTO) {
        return userFollowService.getFollowPage(pageDTO);
    }

    /**
     * 是否关注
     *
     * @param userId
     * @return
     */
    @GetMapping("/weatherfollow/{userId}")
    public R<Boolean> weatherfollow(@PathVariable("userId") Long userId) {

        Long cout = userFollowRepository.countByUserIdOrUserFollowId(UserContext.getUserId(), userId);
        return R.ok(cout > 0);
    }

    /**
     * 根据用户id查询该用户的关注和粉丝数量
     *
     * @param userId
     * @return
     */
    @GetMapping("/followFans/{userId}")
    public R<UserFollowsFansVo> followAndFans(@PathVariable("userId") Long userId) {
        UserFollowsFansVo userFollowsFansVo = new UserFollowsFansVo();
        // 查询关注数量

        Long count = userFollowRepository.countByUserId(userId);
        userFollowsFansVo.setFollowedNums(count);
        // 查询粉丝数

        count = userFollowRepository.countByUserFollowId(userId);
        userFollowsFansVo.setFanNums(count);
        return R.ok(userFollowsFansVo);
    }

    /**
     * 分页查询我的粉丝
     *
     * @param pageDTO
     * @return
     */
    @PostMapping("/fans-page")
    public PageData getUserFansPage(@RequestBody PageDTO pageDTO) {
        return userFollowService.queryUserFansPage(pageDTO);
    }

    /**
     * 初始化收件箱
     */
    @PostMapping("/initVideoFeed")
    public R<?> initFollowFeed() {
        userFollowService.initFollowVideoFeed();
        return R.ok();
    }

    /**
     * 推送关注的人视频 拉模式
     *
     * @param lastTime 滚动分页
     * @return
     */
    @GetMapping("/videoFeed")
    public R followFeed(@RequestParam(required = false) Long lastTime) {
        return R.ok(userFollowService.followVideoFeed(lastTime));
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

}

