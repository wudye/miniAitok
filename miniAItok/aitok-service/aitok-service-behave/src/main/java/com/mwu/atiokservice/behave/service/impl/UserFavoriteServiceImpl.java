package com.mwu.atiokservice.behave.service.impl;


import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitok.VideoIdRequest;
import com.mwu.aitok.VideoResponse;
import com.mwu.aitok.VideoServiceGrpc;
import com.mwu.aitok.model.behave.domain.UserFavorite;
import com.mwu.aitok.model.behave.vo.UserFavoriteInfoVO;
import com.mwu.aitok.model.behave.vo.app.FavoriteFolderVO;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitok.model.common.enums.DelFlagEnum;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.atiokservice.behave.repository.UserFavoriteRepository;
import com.mwu.atiokservice.behave.service.IUserFavoriteVideoService;
import com.mwu.atiokservice.behave.util.PackageCollectionInfoPageProcessor;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * (UserFavorite)表服务实现类
 *
 * @author mwu
 * @since 2023-11-13 16:37:53
 */
@Slf4j
@Service("userFavoriteService")
public class UserFavoriteServiceImpl implements com.mwu.atiokservice.behave.service.IUserFavoriteService {
    @Resource
    private UserFavoriteRepository userFavoriteMapper;

    @Resource
    PackageCollectionInfoPageProcessor packageCollectionInfoPageProcessor;

    @Resource
    private IUserFavoriteVideoService userFavoriteVideoService;


    @GrpcClient("aitok-video")
    private VideoServiceGrpc.VideoServiceBlockingStub videoServiceBlockingStub;

    /**
     * 用户新建收藏夹
     *
     * @param userFavorite
     * @return
     */
    @Override
    public boolean saveFavorite(UserFavorite userFavorite) {
        //从token中获取userId
        userFavorite.setUserId(UserContext.getUserId());
        //从枚举类中获取默认的删除标志参数
        userFavorite.setDelFlag(DelFlagEnum.EXIST.getCode());
        userFavorite.setCreateTime(LocalDateTime.now());
        userFavoriteMapper.save(userFavorite);
        return true;
    }

    /**
     * 分页查询用户收藏夹
     *
     * @param pageDTO
     * @return
     */
    @Override
    public Page<UserFavorite> queryCollectionPage(PageDTO pageDTO) {
        Pageable pageable = PageRequest.of(
                Math.max(0, pageDTO.getPageNum() - 1),
                pageDTO.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );
        Page<UserFavorite> userFavorites = userFavoriteMapper.findAllByUserId(UserContext.getUserId(), pageable);
        return userFavorites;

    }

    /**
     * 我的收藏夹集合详情分页查询
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData queryMyCollectionInfoPage(PageDTO pageDTO) {


        Pageable pageable = PageRequest.of(
                Math.max(0, pageDTO.getPageNum() - 1),
                pageDTO.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime")
        );
        Page<UserFavorite> userFavoritePage = userFavoriteMapper.findAllByUserId(UserContext.getUserId(), pageable);
        List<UserFavorite> records = userFavoritePage.getContent();
        if (records.isEmpty()) {
            return PageData.emptyPage();
        }
        List<UserFavoriteInfoVO> collectionInfoList = BeanCopyUtils.copyBeanList(records, UserFavoriteInfoVO.class);
        // 封装VO
        packageCollectionInfoPageProcessor.processUserFavoriteInfoList(collectionInfoList);
        return PageData.genPageData(collectionInfoList, userFavoritePage.getTotalElements());
    }

    /**
     * 查询收藏集详情
     */
    @Override
    public List<UserFavoriteInfoVO> queryCollectionInfoList() {
        // 1、先获取用户收藏夹集合
        Long userId = UserContext.getUserId();

        List<UserFavorite> collectionList = userFavoriteMapper.findByUserId(userId);
        // 2、遍历获取收藏的视频总数与前六张封面
        List<UserFavoriteInfoVO> collectionInfoList = new ArrayList<>();
        collectionList.forEach(favorite -> {
            UserFavoriteInfoVO favoriteFolderVO = new UserFavoriteInfoVO();
            BeanUtils.copyProperties(favorite, favoriteFolderVO);
            collectionInfoList.add(favoriteFolderVO);
        });

        collectionInfoList.forEach(c -> {
            // 2.1、获取视频总数
            c.setVideoCount(userFavoriteMapper.countById(c.getFavoriteId()));
            // 2.2、获取前六张封面

            // get list video cover_image from video_service by favorite_id

            Long userIdi = c.getUserId();
            List<Long> favoriteVideos = userFavoriteMapper.findByIdOrderByCreateTimeDesc(userIdi);
            List<Long> favoriteVideoIds = favoriteVideos.size() <= 6
                    ? new ArrayList<>(favoriteVideos)
                    : new ArrayList<>(favoriteVideos.subList(0, 6));

            List<String> videoCovers = new ArrayList<>();


            for (Long videoId : favoriteVideoIds) {
                VideoIdRequest videoIdRequest = VideoIdRequest.newBuilder().setVideoId(videoId).build();

                VideoResponse videoResponse = videoServiceBlockingStub.apiGetVideoByVideoId(videoIdRequest);
                Video video = BeanCopyUtils.copyBean(videoResponse, Video.class);
                videoCovers.add(video.getCoverImage());
            }

            String[] videoCoverArray = new String[videoCovers.size()];
            videoCovers.toArray(videoCoverArray);
           c.setVideoCoverList(videoCoverArray);
        });
        return collectionInfoList;
    }

    /**
     * 收藏夹列表
     */
    @Override
    public List<FavoriteFolderVO> userFavoritesFolderList(String videoId) {

        Long userId = UserContext.getUserId();
        List<UserFavorite> favoriteList = userFavoriteMapper.findByUserId(userId);
        if (favoriteList.isEmpty()) {
            return List.of();
        }

        List<FavoriteFolderVO> res = new ArrayList<>();
        favoriteList.forEach(favorite -> {
            FavoriteFolderVO favoriteFolderVO = new FavoriteFolderVO();
            BeanUtils.copyProperties(favorite, favoriteFolderVO);
            // 是否收藏该视频
            favoriteFolderVO.setWeatherFavorite(userFavoriteVideoService.videoWeatherInFavoriteFolder(favorite.getId(), videoId));
            res.add(favoriteFolderVO);
        });


        return res;
    }
}
