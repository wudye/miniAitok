package com.mwu.atiokservice.behave.service.impl;


import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitok.model.behave.domain.UserFavoriteVideo;
import com.mwu.aitok.model.behave.domain.VideoUserFavorites;
import com.mwu.aitok.model.behave.dto.UserFavoriteVideoDTO;
import com.mwu.aitok.model.constants.VideoCacheConstants;
import com.mwu.aitok.model.notice.domain.Notice;
import com.mwu.aitok.model.notice.enums.NoticeType;
import com.mwu.aitok.model.notice.enums.ReceiveFlag;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.atiokservice.behave.repository.UserFavoriteVideoRepository;
import com.mwu.atiokservice.behave.repository.VideoUserFavoritesRepository;
import com.mwu.atiokservice.behave.repository.VideoUserLikeRepository;
import com.mwu.atiokservice.behave.service.IUserFavoriteVideoService;
import com.mwu.atiokservice.behave.service.IVideoUserFavoritesService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.mwu.aitok.model.notice.mq.NoticeDirectConstant.NOTICE_DIRECT_EXCHANGE;


/**
 * (UserFavoriteVideo)表服务实现类
 *
 * @author mwu
 * @since 2023-11-17 10:16:09
 */
@Slf4j
@Service("userFavoriteVideoService")
public class UserFavoriteVideoServiceImpl implements IUserFavoriteVideoService {
    @Resource
    private UserFavoriteVideoRepository userFavoriteVideoMapper;

    @Resource
    private RedisService redisService;

    @Resource
    private VideoUserLikeRepository videoUserLikeMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    @Lazy // 解决循环依赖问题
    private IVideoUserFavoritesService videoUserFavoritesService;



    /**
     * 收藏视频到收藏夹
     *
     * @param userFavoriteVideoDTO
     * @return
     */
    @Transactional(rollbackFor = CustomException.class)
    @Override
    public Boolean videoFavorites(UserFavoriteVideoDTO userFavoriteVideoDTO) {

        /*
        // 删除仅仅收藏此视频的一条记录
        videoUserFavoritesService.remove(new LambdaQueryWrapper<VideoUserFavorites>()
                .eq(VideoUserFavorites::getUserId, UserContext.getUserId())
                .eq(VideoUserFavorites::getVideoId, userFavoriteVideoDTO.getVideoId()));
        // 收藏到收藏夹的同时收藏到仅收藏视频
        VideoUserFavorites videoUserFavorite = new VideoUserFavorites();
        videoUserFavorite.setVideoId(userFavoriteVideoDTO.getVideoId());
        videoUserFavorite.setUserId(UserContext.getUserId());
        videoUserFavorite.setCreateTime(LocalDateTime.now());
        videoUserFavoritesService.save(videoUserFavorite);
        //从token中获取userid
        Long userId = UserContext.getUserId();
        //构建查询条件
        LambdaQueryWrapper<UserFavoriteVideo> queryWrapper = new LambdaQueryWrapper<>();
        //查询是否再收藏夹中收藏该视频
        queryWrapper.eq(UserFavoriteVideo::getVideoId, userFavoriteVideoDTO.getVideoId()).eq(UserFavoriteVideo::getUserId, userId);
        List<UserFavoriteVideo> dbList = this.list(queryWrapper);
        // 查询是否已经只收藏视频，若是则不增加redis收藏数
        LambdaQueryWrapper<VideoUserFavorites> vufQW = new LambdaQueryWrapper<>();
        vufQW.eq(VideoUserFavorites::getVideoId, userFavoriteVideoDTO.getVideoId())
                .eq(VideoUserFavorites::getUserId, userId);
        List<VideoUserFavorites> videoUserFavorites = videoUserFavoritesService.list(vufQW);
        if (dbList.isEmpty() && videoUserFavorites.isEmpty()) {
            //将本条收藏信息存储到redis（key为videoId,value为videoUrl）
//            favoriteNumIncrease(userFavoriteVideoDTO.getVideoId());
            // 发送消息到通知
            sendNotice2MQ(userFavoriteVideoDTO.getVideoId(), userId);
        }
        // 获取该用户所有包含该视频的收藏夹id集合
        Long[] oldIds = dbList.stream().map(UserFavoriteVideo::getFavoriteId).toArray(Long[]::new);
        Long[] newIds = userFavoriteVideoDTO.getFavorites();
        if (StringUtils.isNull(newIds)) {
//            favoriteNumDecrease(userFavoriteVideoDTO.getVideoId());
        }
        // 合并新老收藏夹并去重
        Long[] mergedIds = Stream.concat(Arrays.stream(oldIds), Arrays.stream(newIds)).distinct().toArray(Long[]::new);
        //筛选出合并后的数组中含有的元素，但是userFavoriteVideoDTO.getFavorites()中没有的元素
        Long[] deleteResult = Arrays.stream(mergedIds)
                .filter(id -> !Arrays.asList(newIds).contains(id))
                .toArray(Long[]::new);
        //过滤之后数组中含有的值，即为需要删除的元素----取消收藏
        if (StringUtils.isNotEmpty(deleteResult)) {
            LambdaQueryWrapper<UserFavoriteVideo> qw = new LambdaQueryWrapper<>();
            // 需要删除的记录
            qw.in(UserFavoriteVideo::getFavoriteId, deleteResult);
            qw.eq(UserFavoriteVideo::getUserId, userId);
            qw.eq(UserFavoriteVideo::getVideoId, userFavoriteVideoDTO.getVideoId());
            this.remove(qw);
        }
        //筛选出userFavoriteVideoDTO.getFavorites()中含有的元素，但是mergedIds中没有的元素----收藏
        Long[] newResult = Arrays.stream(newIds)
                .filter(id -> !Arrays.asList(oldIds).contains(id))
                .toArray(Long[]::new);
        if (StringUtils.isNotEmpty(newResult)) {
            // 更新用户模型
            List<Long> tagIds = dubboVideoService.apiGetVideoTagIds(userFavoriteVideoDTO.getVideoId());
            dubboVideoService.apiUpdateUserModel(UserModel.buildUserModel(userId, tagIds, 2.0));
            ArrayList<UserFavoriteVideo> userFavoriteVideos = new ArrayList<>();
            for (Long aLong : newResult) {
                UserFavoriteVideo userFavoriteVideo = new UserFavoriteVideo();
                userFavoriteVideo.setUserId(userId);
                userFavoriteVideo.setVideoId(userFavoriteVideoDTO.getVideoId());
                userFavoriteVideo.setFavoriteId(aLong);
                userFavoriteVideo.setCreateTime(LocalDateTime.now());
                userFavoriteVideos.add(userFavoriteVideo);
            }
            return this.saveBatch(userFavoriteVideos);
        } else {
            return false;
        }

         */
        return true;
    }

    /**
     * 用户收藏视频，通知mq
     *
     * @param videoId
     * @param operateUserId
     */
    @Async
    public void sendNotice2MQ(String videoId, Long operateUserId) {
        // 根据视频获取发布者id
        /*
        Video video = videoUserLikeMapper.selectVideoByVideoId(videoId);
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
        notice.setContent("视频被人收藏了0.o");
        notice.setNoticeType(NoticeType.FAVORITE.getCode());
        notice.setReceiveFlag(ReceiveFlag.WAIT.getCode());
        notice.setCreateTime(LocalDateTime.now());
        // notice消息转换为json
        String msg = JSON.toJSONString(notice);
        rabbitTemplate.convertAndSend(NOTICE_DIRECT_EXCHANGE, NOTICE_CREATE_ROUTING_KEY, msg);
        log.debug(" ==> {} 发送了一条消息 ==> {}", NOTICE_DIRECT_EXCHANGE, msg);
        */

    }

    @Async
    public void favoriteNumIncrease(String videoId) {
        redisService.incrementCacheMapValue(VideoCacheConstants.VIDEO_FAVORITE_NUM_MAP_KEY, videoId, 1);
    }

    @Async
    public void favoriteNumDecrease(String videoId) {
        redisService.incrementCacheMapValue(VideoCacheConstants.VIDEO_FAVORITE_NUM_MAP_KEY, videoId, -1);
    }

    /**
     * 视频是否在收藏夹中
     *
     * @param favoriteId
     * @param videoId
     * @return
     */
    @Override
    public Boolean videoWeatherInFavoriteFolder(Long favoriteId, String videoId) {
        /*
        LambdaQueryWrapper<UserFavoriteVideo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFavoriteVideo::getFavoriteId, favoriteId).eq(UserFavoriteVideo::getVideoId, videoId);
        return this.count(queryWrapper) > 0;

         */
        return true;
    }

    @Override
    public List<UserFavoriteVideo> getUserFavoriteVideos(Long userId, String videoId) {
        return List.of();
    }
}
