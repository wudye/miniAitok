package com.mwu.aitok.service.video.controller.v1;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitiokcoomon.core.utils.file.PathUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.model.common.dto.PageDTO;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.dto.*;
import com.mwu.aitok.model.video.vo.VideoUploadVO;
import com.mwu.aitok.model.video.vo.VideoVO;
import com.mwu.aitok.service.video.annotation.VideoRepeatSubmit;
import com.mwu.aitok.service.video.service.IVideoService;
import com.mwu.aitok.service.video.service.InterestPushService;
import com.mwu.aitokcommon.cache.annotations.DoubleCache;
import com.mwu.aitokcommon.cache.annotations.RedissonLock;
import com.mwu.aitokstarter.file.service.MinioService;
import com.mwu.aitokstarter.video.service.FfmpegVideoService;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.info.MultimediaInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 视频表(Video)表控制层
 *
 * @author mwu
 * @since 2023-10-25 20:33:08
 */
// TODO api crud move to /api/v1/creator
@RestController
@RequestMapping("/api/v1")
public class VideoController {

    @Resource
    private IVideoService videoService;



    @Resource
    private MinioService minioService;


    @Resource
    private InterestPushService interestPushService;

    @Resource
    private FfmpegVideoService ffmpegVideoService;



    /**
     * todo 前端在dto传入一个唯一业务字段 #videoPublishDto.uniqueKey，这个唯一key可以使用雪花生成的视频id
     *
     * @param videoPublishDto
     * @return
     */
    @VideoRepeatSubmit(key = "#videoPublishDto.coverImage")
    @PostMapping("/test-video-repeat-submit")
    public R<String> testVideoRepeatSubmit(@RequestBody VideoPublishDto videoPublishDto) {
        return R.ok();
    }

    //    /**
//     * 测试redisson分布式锁
//     *
//     * @return
//     */
//    @GetMapping("/test-redisson-lock")
//    @RedissonLock(prefixKey = "redisson:lock", key = "test")
//    public R<String> testRateLimit() {
//        return R.ok("test rate limit");
//    }

    /**
     * 测试分布式锁
     * 测试二级缓存
     * http://127.0.0.1:9301/api/v1/testRedissonLock?id=123
     *
     * @param id
     * @return
     */
    /*
    在 key = "#id" 中， # 是 SpEL（Spring Expression Language）语法，表示方法参数引用。

SpEL 表达式语法
#id 的含义
# : SpEL前缀，告诉Spring这是一个表达式
id : 方法参数名
     */
    @DoubleCache(cachePrefix = "aaatest:double:cache", key = "#id", expire = 10, unit = TimeUnit.MINUTES)
    @RedissonLock(prefixKey = "aaaredisson:lock", key = "#id")
    @GetMapping("/testRedissonLock")
    public R<String> testRedissonLock(@RequestParam("id") Long id) {
        return R.ok("testRedissonLock");
    }

    /**
     * 首页推送视频
     *
     * @return
     */
    @GetMapping("/pushVideo")
    public R<?> pushVideo() {
        return R.ok(videoService.pushVideoList());
    }

    /**
     * 热门视频
     *
     * @param pageDTO
     * @return
     */
    @PostMapping("/hot")
    @Cacheable(value = "hotVideos", key = "'hotVideos'+#pageDTO.pageNum + '_' + #pageDTO.pageSize")
    public PageData hotVideos(@RequestBody PageDTO pageDTO) {
        return videoService.getHotVideos(pageDTO);
    }

    /**
     * 视频流接口,默认返回10条数据
     */
    @PostMapping("/feed")
    public R<List<VideoVO>> feed(@RequestBody VideoFeedDTO videoFeedDTO) {
        return R.ok(videoService.feedVideo(videoFeedDTO));
    }

    /**
     * 视频上传 todo 上传视频业务转移到creator创作者中心
     */
    @PostMapping("/upload")
    public R<VideoUploadVO> uploadVideo(@RequestParam("file") MultipartFile file) throws Exception {
        /*
        return R.ok(videoService.uploadVideo(file));

         */
        String res = "use creator to upload video /api/v1/creator/upload-video";
        return R.ok(null);
    }

    /**
     * 图片上传
     */
    @Deprecated
    @PostMapping("/upload/image")
    public R<String> uploadImages(@RequestParam("file") MultipartFile file) throws Exception {
        /*
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isNull(originalFilename)) {
            throw new CustomException(HttpCodeEnum.IMAGE_TYPE_FOLLOW);
        }
        //对原始文件名进行判断
        if (originalFilename.endsWith(".png")
                || originalFilename.endsWith(".jpg")
                || originalFilename.endsWith(".jpeg")
                || originalFilename.endsWith(".webp")) {
            String filePath = PathUtils.generateFilePath(originalFilename);
            String url =  minioService.uploadFile(file);
            return R.ok(url);
        } else {
            throw new CustomException(HttpCodeEnum.IMAGE_TYPE_FOLLOW);
        }

         */
        String res = "use creator to upload image /api/v1/creator/upload-video-image";
        return R.ok(res);
    }

    /**
     * 将用户上传的视频和用户信息绑定到一起
     */
    @PostMapping("/publish")
    public R<?> videoPublish(@RequestBody VideoPublishDto videoPublishDto) {
        return R.ok(videoService.videoPublish(videoPublishDto));
    }

    /**
     * 分页查询我的视频
     */
    @PostMapping("/mypage")
    public PageData myPage(@RequestBody VideoPageDto pageDto) {
        return (PageData) videoService.queryMyVideoPage(pageDto);
    }

    /**
     * 分页查询用户视频
     */
    @PostMapping("/userpage")
    public PageData userPage(@RequestBody VideoPageDto pageDto) {
        return (PageData) videoService.queryUserVideoPage(pageDto);
    }

    /**
     * 通过ids获取video集合
     */
    @GetMapping("/videoVO/{videoId}")
    public R<VideoVO> queryVideoVOByVideoId(@PathVariable("videoId") String videoId) {
        return R.ok(videoService.getVideoVOById(videoId));
    }

    /**
     * 通过ids获取video集合
     */
    @GetMapping("{videoIds}")
    public R<List<Video>> queryVideoByVideoIds(@PathVariable("videoIds") List<String> videoIds) {
        return R.ok(videoService.queryVideoByVideoIds(videoIds));
    }

    /**
     * 更新视频 information
     */
    @PutMapping("/update")
    public R<?> updateVideo(@RequestBody UpdateVideoDTO updateVideoDTO) {
        return R.ok(videoService.updateVideo(updateVideoDTO));
    }

    /**
     * 删除视频
     */
    @DeleteMapping("/{videoId}")
    public R<?> deleteVideoByVideoIds(@PathVariable("videoId") String videoId) {
        return R.ok(videoService.deleteVideoByVideoId(videoId));
    }

    /**
     * 用户视频总获赞量
     */
    @GetMapping("/likeNums/{userId}")
    public R<Long> getVideoLikeAllNumByUserId(@PathVariable("userId") Long userId) {
        return R.ok(videoService.getVideoLikeAllNumByUserId(userId));
    }

    /**
     * 查询我的作品数量
     */
    @GetMapping("/videoCount")
    public R<Long> getUserVideoNum() {
        return R.ok(videoService.queryUserVideoCount());
    }

    /**
     * 根据视频远程url获取视频详情
     */
    @PostMapping("/videoinfo")
    public R<?> getVideoInfo(@RequestBody VideoInfoDTO videoInfoDTO) {
        MultimediaInfo info = ffmpegVideoService.getVideoInfo(videoInfoDTO.getVideoUrl());
        return R.ok(info);
    }

    /**
     * 相关视频推荐
     */
    @GetMapping("/relate/{videoId}")
    public R<?> getRelateVideo(@PathVariable("videoId") String videoId) {
        return R.ok(videoService.getRelateVideoList(videoId));
    }

}
