package com.mwu.atiokservice.behave.controller.v1;



import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.behave.domain.UserFavorite;
import com.mwu.aitok.model.behave.vo.UserFavoriteInfoVO;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.atiokservice.behave.repository.UserFavoriteRepository;
import com.mwu.atiokservice.behave.service.IUserFavoriteService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


/**
 * (UserFavorite)表控制层
 *
 * @author mwu
 * @since 2023-11-13 16:37:53
 */
@RestController
@RequestMapping("/api/v1/userFavorite")
public class UserFavoriteController {

    @Resource
    private IUserFavoriteService userFavoriteService;

    @Resource
    private UserFavoriteRepository userFavoriteMapper;

    /**
     * 我的收藏夹集合
     */
    @GetMapping("/list")
    public R<?> userFavoriteList() {


        Long userId = UserContext.getUserId();

        if (userId == null) {
            return R.fail("用户未登录");
        }

        return R.ok(userFavoriteMapper.findByUserId(userId));
    }

    /**
     * 我的收藏夹集合分页查询
     */
    @PostMapping("/page")
    public PageData userFavoritePage(@RequestBody PageDTO pageDTO) {
        Page<UserFavorite> userFavoriteIPage = userFavoriteService.queryCollectionPage(pageDTO);
        return PageData.genPageData(userFavoriteIPage.getContent(), userFavoriteIPage.getTotalElements());
    }

    /**
     * 我的收藏夹详情集合
     */
    @GetMapping("/infoList")
    public R<List<UserFavoriteInfoVO>> userCollectionInfoList() {
        return R.ok(userFavoriteService.queryCollectionInfoList());
    }

    /**
     * 我的收藏夹集合详情分页查询
     */
    @PostMapping("/infoPage")
    public  PageData userCollectionInfoPage(@RequestBody PageDTO pageDTO) {
        return userFavoriteService.queryMyCollectionInfoPage(pageDTO);
    }

    /**
     * 新建收藏夹
     */
    @PostMapping()
    public R<?> newFavorite(@RequestBody UserFavorite userFavorite) {
        userFavorite.setUserId(UserContext.getUserId());
        return R.ok(userFavoriteService.saveFavorite(userFavorite));
    }

    /**
     * 根据id获取
     *
     * @param favoriteId
     * @return
     */
    @GetMapping("/{favoriteId}")
    public R<UserFavorite> getInfoById(@PathVariable("favoriteId") Long favoriteId) {


        UserFavorite userFavorite = userFavoriteMapper.findByUserIdAndId(UserContext.getUserId(), favoriteId);
        if (userFavorite == null) {
            return R.fail("收藏夹不存在或无权限查看");
        }
        return R.ok(userFavorite);
    }

    /**
     * 更新收藏夹
     */
    @PutMapping()
    public R<Boolean> updateFavorite(@RequestBody UserFavorite userFavorite) {
        UserFavorite existingFavorite = userFavoriteMapper.findById(userFavorite.getId()).orElse(null);
        if (existingFavorite == null || !existingFavorite.getUserId().equals(UserContext.getUserId())) {
            return R.fail("收藏夹不存在或无权限修改");
        }

        userFavorite.setUserId(UserContext.getUserId());
        userFavoriteMapper.save(userFavorite);

        return R.ok(true);
    }

    /**
     * 删除收藏夹
     *
     * @param favoriteId
     * @return
     */
    @DeleteMapping("/{favoriteId}")
    public R<Boolean> deleteFavorite(@PathVariable("favoriteId") Long favoriteId) {
        userFavoriteMapper.deleteById(favoriteId);
        return R.ok(true);
    }

}

