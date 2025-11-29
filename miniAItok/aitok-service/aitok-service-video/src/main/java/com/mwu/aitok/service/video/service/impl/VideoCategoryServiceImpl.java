package com.mwu.aitok.service.video.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.GetByIdRequest;
import com.mwu.aitok.MemberResponse;
import com.mwu.aitok.MemberServiceGrpc;
import com.mwu.aitok.model.constants.VideoCacheConstants;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.domain.VideoCategory;
import com.mwu.aitok.model.video.domain.VideoImage;
import com.mwu.aitok.model.video.dto.CategoryVideoPageDTO;
import com.mwu.aitok.model.video.dto.VideoCategoryPageDTO;
import com.mwu.aitok.model.video.enums.PublishType;
import com.mwu.aitok.model.video.enums.VideoCategoryStatus;
import com.mwu.aitok.model.video.vo.*;
import com.mwu.aitok.model.video.vo.app.AppVideoCategoryVo;
import com.mwu.aitok.model.video.vo.app.CategoryVideoVo;
import com.mwu.aitok.service.video.repository.VideoCategoryRelationRepository;
import com.mwu.aitok.service.video.repository.VideoCategoryRepository;
import com.mwu.aitok.service.video.repository.VideoRepository;
import com.mwu.aitok.service.video.service.IVideoCategoryRelationService;
import com.mwu.aitok.service.video.service.IVideoCategoryService;
import com.mwu.aitok.service.video.service.IVideoImageService;
import com.mwu.aitok.service.video.service.IVideoService;
import com.mwu.aitok.service.video.service.cache.VideoRedisBatchCache;
import com.mwu.aitokcommon.cache.annotations.DoubleCache;
import com.mwu.aitokcommon.cache.enums.CacheType;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitolk.feign.behave.RemoteBehaveService;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.mwu.aitok.model.constants.VideoCacheConstants.VIDEO_IMAGES_PREFIX_KEY;
import static com.mwu.aitok.service.video.constants.InterestPushConstant.VIDEO_CATEGORY_VIDEOS_CACHE_KEY_PREFIX;

/**
 * (VideoCategory)表服务实现类
 *
 * @author mwu
 * @since 2023-10-30 19:41:14
 */
@Slf4j
@Service
public class VideoCategoryServiceImpl implements IVideoCategoryService {
    @Resource
    private VideoCategoryRepository videoCategoryMapper;

    @Resource
    private RedisService redisService;

    @Resource
    private IVideoCategoryRelationService videoCategoryRelationService;

    @Resource
    private IVideoService videoService;

    @Resource
    private VideoRepository videoMapper;

    @Resource
    private IVideoImageService videoImageService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RemoteBehaveService videoUserLikeRepository;

    @Autowired
    private VideoRepository videoRepository;


    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceGrpc;
    private static final long TIMEOUT_MS = 2000L;



    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private VideoRedisBatchCache videoRedisBatchCache;

    @Autowired
    private VideoCategoryRelationRepository videoCategoryRelationRepository;

    @Override
    public List<VideoCategory> saveVideoCategoriesToRedis() {
        // 查询数据库获取视频分类列表
        List<VideoCategory> videoCategories = videoCategoryMapper.findAllByIdOrParentId(0L, 0L);
        if (videoCategories.isEmpty()) {
            return new ArrayList<>();
        }
        redisService.setCacheList(VideoCacheConstants.VIDEO_CATEGORY_PREFIX, videoCategories);
        return videoCategories;
    }

    /**
     * 获取所有的分类列表
     */
    @Override
    public List<VideoCategoryVo> selectAllCategory() {

        List<VideoCategory> cacheList = redisService.getCacheList(VideoCacheConstants.VIDEO_CATEGORY_PREFIX);
        if (cacheList.isEmpty()) {
            cacheList = saveVideoCategoriesToRedis();
        }
        return BeanCopyUtils.copyBeanList(cacheList, VideoCategoryVo.class);
    }

    @Override
    public List<VideoCategoryVo> selectAllParentCategory() {
        List<VideoCategory> cacheList = redisService.getCacheList(VideoCacheConstants.VIDEO_CATEGORY_PREFIX);
        if (cacheList.isEmpty()) {
            cacheList = saveVideoCategoriesToRedis();
        }
        return BeanCopyUtils.copyBeanList(cacheList, VideoCategoryVo.class);
    }

    /**
     * 分页根据分类获取视频
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData selectVideoByCategory(VideoCategoryPageDTO pageDTO) {
        if (StringUtils.isNull(pageDTO.getCategoryId())) {
            return PageData.emptyPage();
        }
        // 查询该分类id以及其子分类
        pageDTO.setPageNum((pageDTO.getPageNum() - 1) * pageDTO.getPageSize());
        Long categoryId = pageDTO.getCategoryId();
        List<Long> videoId = videoCategoryRelationRepository.getVideoIdByCategoryId(categoryId);
        if (CollectionUtils.isEmpty(videoId)) {
            return PageData.emptyPage();
        }

        Pageable pageable = PageRequest.of(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<Video> videoPage = videoMapper.findAllByIdIn(videoId, pageable);


        List<Video> videoList = videoPage.getContent();
        List<VideoVO> videoVOList = BeanCopyUtils.copyBeanList(videoList, VideoVO.class);
        Long videoCount = videoCategoryMapper.countByCategoryOrChildren(pageDTO.getCategoryId());
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(videoVOList
                .stream()
                .map(this::packageUserVideoVOAsync)
                .toArray(CompletableFuture[]::new));
        allFutures.join();
        return PageData.genPageData(videoList, videoCount);
    }

    public CompletableFuture<Void> packageUserVideoVOAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageUserVideoVO(videoVO));
    }

    public void packageUserVideoVO(VideoVO videoVO) {
        CompletableFuture<Void> behaveDataFuture = packageVideoBehaveDataAsync(videoVO);
        CompletableFuture<Void> memberDataFuture = packageMemberDataAsync(videoVO);
        CompletableFuture<Void> imageDataFuture = packageVideoImageDataAsync(videoVO);
//        CompletableFuture<Void> positionDataFuture = packageVideoPositionDataAsync(videoVO);
        CompletableFuture.allOf(
                behaveDataFuture,
                memberDataFuture,
                imageDataFuture
        ).join();
    }

    public CompletableFuture<Void> packageVideoBehaveDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoBehaveData(videoVO));
    }

    public CompletableFuture<Void> packageMemberDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageMemberData(videoVO));
    }

    public CompletableFuture<Void> packageVideoImageDataAsync(VideoVO videoVO) {
        return CompletableFuture.runAsync(() -> packageVideoImageData(videoVO));
    }

    /**
     * 封装视频行为数据
     *
     * @param videoVO
     */
    public void packageVideoBehaveData(VideoVO videoVO) {
        log.debug("packageVideoBehaveData开始");
        // 封装观看量、点赞数、收藏量 todo java.lang.IllegalArgumentException: non null hash key required
        Integer cacheViewNum = redisService.getCacheMapValue(VideoCacheConstants.VIDEO_VIEW_NUM_MAP_KEY, String.valueOf(videoVO.getId()));
        videoVO.setViewNum(StringUtils.isNull(cacheViewNum) ? 0L : cacheViewNum);
        videoVO.setLikeNum(videoMapper.getLikeCountById(videoVO.getId()));
        videoVO.setFavoritesNum(videoMapper.getFavoritesCountById(videoVO.getId()));
        // 评论数
        videoVO.setCommentNum(videoMapper.getCommentNumById(videoVO.getId()));
        log.debug("packageVideoBehaveData结束");
    }

    /**
     * 封装用户数据
     *
     * @param videoVO
     */
    public void packageMemberData(VideoVO videoVO) {
        log.debug("packageMemberData开始");
        // 封装用户信息
        Member userCache = redisService.getCacheObject("member:userinfo:" + videoVO.getUserId());
        if (StringUtils.isNotNull(userCache)) {
            videoVO.setUserNickName(userCache.getNickName());
            videoVO.setUserAvatar(userCache.getAvatar());
        } else {

            MemberResponse memberResponse = memberServiceGrpc.getById(GetByIdRequest.newBuilder().setUserId(videoVO.getUserId()).build());
            Member member = BeanCopyUtils.copyBean(Objects.isNull(memberResponse) ? new Member() : memberResponse, Member.class);
            if (StringUtils.isNotNull(member)) {
                videoVO.setUserNickName(member.getNickName());
                videoVO.setUserAvatar(member.getAvatar());
                return;
            }
        }

        log.debug("packageMemberData结束");
    }

    /**
     * 封装图文数据
     *
     * @param videoVO
     */
    public void packageVideoImageData(VideoVO videoVO) {
        log.debug("packageVideoImageData开始");
        if (videoVO.getPublishType().equals(PublishType.IMAGE.getCode())) {
            Object imgsCacheObject = redisService.getCacheObject(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId());
            if (StringUtils.isNotNull(imgsCacheObject)) {
                try {
                    if (imgsCacheObject instanceof List) {
                        List<?> list = (List<?>) imgsCacheObject;
                        videoVO.setImageList(list.stream().map(Object::toString).toArray(String[]::new));
                    } else if (imgsCacheObject instanceof String) {
                        String jsonString = (String) imgsCacheObject;
                        String[] imgs = objectMapper.readValue(jsonString, String[].class);
                        videoVO.setImageList(imgs);
                    } else {
                        // fallback: try to convert via ObjectMapper
                        String json = objectMapper.writeValueAsString(imgsCacheObject);
                        String[] imgs = objectMapper.readValue(json, String[].class);
                        videoVO.setImageList(imgs);
                    }
                } catch (Exception e) {
                    log.warn("解析缓存图片列表失败，采用 DB 数据回退", e);
                }
            }
            if (videoVO.getImageList() == null || videoVO.getImageList().length == 0) {
                List<VideoImage> videoImageList = videoImageService.queryImagesByVideoId(String.valueOf(videoVO.getId()));
                String[] imgs = videoImageList.stream().map(VideoImage::getImageUrl).toArray(String[]::new);
                videoVO.setImageList(imgs);
                try {
                    redisService.setCacheObject(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId(), objectMapper.writeValueAsString(imgs));
                    redisService.expire(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId(), 1, TimeUnit.DAYS);
                } catch (JsonProcessingException e) {
                    log.warn("序列化图片列表写缓存失败，回退写原始对象", e);
                    redisService.setCacheObject(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId(), imgs);
                    redisService.expire(VIDEO_IMAGES_PREFIX_KEY + videoVO.getId(), 1, TimeUnit.DAYS);
                }
            }
        }
        log.debug("packageVideoImageData结束");
    }
    @Override
    public List<VideoPushVO> pushVideoByCategory(Long categoryId) {
        // 先判断 categoryId 是否有效

        List<VideoCategory> categoryList = videoCategoryMapper.findByIdOrParentId(categoryId, categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            return new ArrayList<>();
        }
//        String categoryKey = VIDEO_CATEGORY_VIDEOS_CACHE_KEY_PREFIX + categoryId;
//        String pushedKey = VIDEO_CATEGORY_PUSHED_CACHE_KEY_PREFIX + UserContext.getUserId();
//        Long totalCount = redisTemplate.opsForSet().size(categoryKey);
//        Long pushedCount = redisTemplate.opsForSet().size(pushedKey);
//        if (StringUtils.isNull(totalCount) || totalCount < 1) {
//            log.debug("没有分类视频");
//        }
//        Long subCount = totalCount - pushedCount;
//        if (subCount < 1) {
//            return new ArrayList<>();
//        }
        // 查询当前用户已推送历史
//        Set<Object> cacheSet = redisService.getCacheSet(pushedKey);
        // 去重结果
        Set<String> results = new HashSet<>(20);
        // 随机获取10条记录
//        while (results.size() < 10) {
//            String item = (String) redisTemplate.opsForSet().randomMember(categoryKey);
//            if (!cacheSet.contains(item)) {
//                // 筛选出未被推送过的数据
//                results.add(item);
//                cacheSet.add(item);
//                // 已推送记录存到redis，过期时间为1小时，可以封装为异步
//                redisTemplate.opsForSet().add(pushedKey, item);
//                redisService.expire(pushedKey, 1, TimeUnit.MINUTES);
//            }
//            if (results.size() >= subCount) {
//                break;
//            }
//        }
        List<Long> categoryIds = categoryList.stream().map(VideoCategory::getId).collect(Collectors.toList());
        categoryIds.forEach(id -> {
            List<String> strings = pullVideoIdsByCategoryId(id);
            results.addAll(strings);
        });
        // 封装result
//        List<Video> videoList = videoService.listByIds(results);
        Map<String, Video> batch = videoRedisBatchCache.getBatch(new ArrayList<>(results));
        List<Video> videoList = new ArrayList<>(batch.values());
        List<VideoPushVO> videoPushVOList = BeanCopyUtils.copyBeanList(videoList, VideoPushVO.class);
        CompletableFuture.allOf(videoPushVOList.stream()
                .map(videoPushVO -> CompletableFuture.runAsync(() -> {
                    asyncPackageAuthor(videoPushVO);
                    asyncPackageVideoImage(videoPushVO);
                })).toArray(CompletableFuture[]::new)).join();
        return videoPushVOList;
    }

    public List<String> pullVideoIdsByCategoryId(Long categoryId) {
        String categoryKey = VIDEO_CATEGORY_VIDEOS_CACHE_KEY_PREFIX + categoryId;
        List<Object> list = redisTemplate.opsForSet().randomMembers(categoryKey, 10);
        return list.stream().map(Object::toString).collect(Collectors.toList());
    }

    /**
     * 封装视频作者
     */
    public void asyncPackageAuthor(VideoPushVO videoPushVO) {

        MemberResponse memberResponse = memberServiceGrpc.getById(GetByIdRequest.newBuilder().setUserId(videoPushVO.getUserId()).build());
        Member member = BeanCopyUtils.copyBean(Objects.isNull(memberResponse) ? new Member() : memberResponse, Member.class);

        Author author = BeanCopyUtils.copyBean(member, Author.class);
        videoPushVO.setAuthor(author);
    }

    /**
     * 封装视频图文
     */
    public void asyncPackageVideoImage(VideoPushVO videoPushVO) {
        // 封装图文
        if (videoPushVO.getPublishType().equals(PublishType.IMAGE.getCode())) {
            Object imgsCacheObject = redisService.getCacheObject(VIDEO_IMAGES_PREFIX_KEY + videoPushVO.getVideoId());
            if (StringUtils.isNotNull(imgsCacheObject)) {
                try {
                    if (imgsCacheObject instanceof List) {
                        List<?> list = (List<?>) imgsCacheObject;
                        videoPushVO.setImageList(list.stream().map(Object::toString).toArray(String[]::new));
                    } else if (imgsCacheObject instanceof String) {
                        String jsonString = (String) imgsCacheObject;
                        String[] imgs = objectMapper.readValue(jsonString, String[].class);
                        videoPushVO.setImageList(imgs);
                    } else {
                        String json = objectMapper.writeValueAsString(imgsCacheObject);
                        String[] imgs = objectMapper.readValue(json, String[].class);
                        videoPushVO.setImageList(imgs);
                    }
                } catch (Exception e) {
                    log.warn("解析缓存图片列表失败，采用 DB 数据回退", e);
                }
            }
            if (videoPushVO.getImageList() == null || videoPushVO.getImageList().length == 0) {
                List<VideoImage> videoImageList = videoImageService.queryImagesByVideoId(videoPushVO.getVideoId());
                String[] imgs = videoImageList.stream().map(VideoImage::getImageUrl).toArray(String[]::new);
                videoPushVO.setImageList(imgs);
                try {
                    redisService.setCacheObject(VIDEO_IMAGES_PREFIX_KEY + videoPushVO.getVideoId(), objectMapper.writeValueAsString(imgs));
                    redisService.expire(VIDEO_IMAGES_PREFIX_KEY + videoPushVO.getVideoId(), 1, TimeUnit.DAYS);
                } catch (JsonProcessingException e) {
                    log.warn("序列化图片列表写缓存失败，回退写原始对象", e);
                    redisService.setCacheObject(VIDEO_IMAGES_PREFIX_KEY + videoPushVO.getVideoId(), imgs);
                    redisService.expire(VIDEO_IMAGES_PREFIX_KEY + videoPushVO.getVideoId(), 1, TimeUnit.DAYS);
                }
            }
        }
    }

    /**
     * 获取视频分类树
     */
    @DoubleCache(cachePrefix = "video:category:tree", expire = 1, unit = TimeUnit.DAYS, type = CacheType.FULL)
    @Override
    public List<VideoCategoryTree> getCategoryTree() {

        List<VideoCategory> videoCategoryList = videoCategoryMapper
                .findByStatusAndVisibleAfterOrderByOrderNumAsc(VideoCategoryStatus.NORMAL.getCode(),
                        VideoCategoryStatus.NORMAL.getCode()
                );

        List<VideoCategoryTree> videoCategoryTrees = BeanCopyUtils.copyBeanList(videoCategoryList, VideoCategoryTree.class);
        return buildVideoCategoryTree(videoCategoryTrees);
    }

    /**
     * 构造树
     *
     * @param videoCategoryTrees
     * @return
     */
    public List<VideoCategoryTree> buildVideoCategoryTree(List<VideoCategoryTree> videoCategoryTrees) {
        List<VideoCategoryTree> rootCategories = new ArrayList<>();

        // 使用 Map 存储分类 ID 和对应的分类树节点
        Map<Long, VideoCategoryTree> categoryMap = new HashMap<>();
        for (VideoCategoryTree categoryTree : videoCategoryTrees) {
            categoryMap.put(categoryTree.getId(), categoryTree);
        }

        for (VideoCategoryTree categoryTree : videoCategoryTrees) {
            Long parentId = categoryTree.getParentId();
            if (parentId == null || parentId == 0) {
                // 根节点
                rootCategories.add(categoryTree);
            } else {
                // 非根节点，将当前节点添加到父节点的 children 列表中
                VideoCategoryTree parentCategory = categoryMap.get(parentId);
                if (parentCategory != null) {
                    List<VideoCategory> children = parentCategory.getChildren();
                    if (children == null) {
                        children = new ArrayList<>();
                        parentCategory.setChildren(children);
                    }
                    children.add(categoryTree);
                }
            }
        }

        return rootCategories;
    }

    /**
     * 获取所有可用视频父分类
     */
    @DoubleCache(cachePrefix = "video:category:parent_list", expire = 1, unit = TimeUnit.HOURS)
    @Override
    public List<AppVideoCategoryVo> getNormalParentCategory() {

        List<VideoCategory> videoCategoryList = videoCategoryMapper
                .findByParentIdAndStatusOrderByOrderNumAsc(0L, VideoCategoryStatus.NORMAL.getCode());
        return BeanCopyUtils.copyBeanList(videoCategoryList, AppVideoCategoryVo.class);
    }

    /**
     * 获取一级子分类
     *
     * @param id
     * @return
     */
    @DoubleCache(cachePrefix = "video:category:children", key = "#id", expire = 1, unit = TimeUnit.HOURS)
    @Override
    public List<AppVideoCategoryVo> getNormalChildrenCategory(Long id) {
        VideoCategory byId = videoCategoryMapper.findById(id).orElse(null);
        if (byId == null) {
            throw new RuntimeException("分类不存在");
        }
        if (byId.getStatus().equals(VideoCategoryStatus.DISABLE.getCode())) {
            throw new RuntimeException("已" + VideoCategoryStatus.DISABLE.getCode());
        }
        List<VideoCategory> videoCategoryList = videoCategoryMapper
                .findByParentIdAndStatusOrderByOrderNumAsc(id, VideoCategoryStatus.NORMAL.getCode());
        return BeanCopyUtils.copyBeanList(videoCategoryList, AppVideoCategoryVo.class);
    }

    /**
     * 根据分类id分页获取视频
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData getVideoPageByCategoryId(CategoryVideoPageDTO pageDTO) {
        pageDTO.setPageNum((pageDTO.getPageNum() - 1) * pageDTO.getPageSize());
        Pageable pageable = PageRequest.of(pageDTO.getPageNum(), pageDTO.getPageSize());
        // Specification 替代 LambdaQueryWrapper
        Specification<Video> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("id"), pageDTO.getId()));
            predicates.add(cb.equal(root.get("delFlag"),0)  );
            query.orderBy(cb.desc(root.get("createTime")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Video> page = videoMapper.findAll(spec, pageable);
        List<Video> videoList = page.getContent();



        List<CategoryVideoVo> categoryVideoVoList = BeanCopyUtils.copyBeanList(videoList, CategoryVideoVo.class);
        // 设置属性
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(categoryVideoVoList.stream()
                .map(this::packageCategoryVideoVoAsync).toArray(CompletableFuture[]::new));
        allFutures.join();

        int cou = videoCategoryRelationRepository.countByCategoryId(pageDTO.getId());
        return PageData.genPageData(categoryVideoVoList, ((long) cou));
    }

    public CompletableFuture<Void> packageCategoryVideoVoAsync(CategoryVideoVo vo) {
        return CompletableFuture.runAsync(() -> packageCategoryVideoVo(vo));
    }

    public void packageCategoryVideoVo(CategoryVideoVo vo) {
        // 点赞量
        CompletableFuture<Void> behaveDataFuture = packageCategoryVideoVoBehaveDataAsync(vo);
        // 作者
        CompletableFuture<Void> memberDataFuture = packageCategoryVideoVoMemberDataAsync(vo);
        CompletableFuture.allOf(
                behaveDataFuture,
                memberDataFuture
        ).join();
    }


    public CompletableFuture<Void> packageCategoryVideoVoBehaveDataAsync(CategoryVideoVo vo) {
        return CompletableFuture.runAsync(() -> packageCategoryVideoVoBehaveData(vo));
    }

    public CompletableFuture<Void> packageCategoryVideoVoMemberDataAsync(CategoryVideoVo vo) {
        return CompletableFuture.runAsync(() -> packageCategoryVideoVoMemberData(vo));
    }


    /**
     * 封装视频行为数据
     */
    public void packageCategoryVideoVoBehaveData(CategoryVideoVo vo) {
        // 封装观看量、点赞数、收藏量 todo java.lang.IllegalArgumentException: non null hash key required
        Integer cacheViewNum = redisService.getCacheMapValue(VideoCacheConstants.VIDEO_VIEW_NUM_MAP_KEY, vo.getVideoId());
        vo.setViewNum(StringUtils.isNull(cacheViewNum) ? 0L : cacheViewNum);
        vo.setLikeNum(videoUserLikeRepository.getLikeNumByVideoId(Long.valueOf((vo.getVideoId()))).getData());
    }

    /**
     * 封装用户数据
     */
    public void packageCategoryVideoVoMemberData(CategoryVideoVo vo) {
        // 封装用户信息
        MemberResponse memberResponse = memberServiceGrpc

                             .withDeadlineAfter(TIMEOUT_MS, TimeUnit.MILLISECONDS)

                .getById(GetByIdRequest.newBuilder().setUserId(vo.getUserId()).build());
        Member member = BeanCopyUtils.copyBean(memberResponse, Member.class);
        Author author = BeanCopyUtils.copyBean(Objects.isNull(member) ? new Member() : member, Author.class);
        vo.setAuthor(author);
    }

    /**
     * 获取视频父分类集合
     * 添加二级缓存
     *
     * @return
     */
    @DoubleCache(cachePrefix = "video:category:parent_list", expire = 1, unit = TimeUnit.HOURS)
    @Override
    public List<VideoCategory> getVideoParentCategoryList() {
        String parentCategoryKey = VideoCategoryStatus.NORMAL.getCode();

        return videoCategoryMapper.findByStatusAndParentId(parentCategoryKey, 0L);

    }
}
