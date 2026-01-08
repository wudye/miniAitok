package com.mwu.atiokservice.behave.service;



import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.behave.domain.UserFavorite;
import com.mwu.aitok.model.behave.vo.UserFavoriteInfoVO;
import com.mwu.aitok.model.behave.vo.app.FavoriteFolderVO;
import com.mwu.aitok.model.common.dto.PageDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * (UserFavorite)表服务接口
 *
 * @author mwu
 * @since 2023-11-13 16:37:53
 */
public interface IUserFavoriteService {

    /**
     * 用户新建收藏夹
     *
     * @param userFavorite
     * @return
     */
    boolean saveFavorite(UserFavorite userFavorite);

    /**
     * 查询收藏集详情
     *
     * @return
     */
    List<UserFavoriteInfoVO> queryCollectionInfoList();

    /**
     * 分页查询用户收藏夹
     *
     * @param pageDTO
     * @return
     */
    Page<UserFavorite> queryCollectionPage(PageDTO pageDTO);

    /**
     * 我的收藏夹集合详情分页查询
     *
     * @param pageDTO
     * @return
     */
   PageData queryMyCollectionInfoPage(PageDTO pageDTO);

    /**
     * 收藏夹列表
     */
    List<FavoriteFolderVO> userFavoritesFolderList(String videoId);

}
