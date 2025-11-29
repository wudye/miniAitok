package com.mwu.aitokservice.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitiokcoomon.core.utils.date.DateUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.*;
import com.mwu.aitok.model.common.enums.VideoPlatformEnum;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.search.ESIndexConstants;
import com.mwu.aitok.model.search.dto.PageDTO;
import com.mwu.aitok.model.search.dto.VideoSearchKeywordDTO;
import com.mwu.aitok.model.search.dto.VideoSearchSuggestDTO;
import com.mwu.aitok.model.search.enums.VideoSearchScreenPublishTime;
import com.mwu.aitok.model.search.vo.VideoSearchVO;
import com.mwu.aitok.model.search.vo.app.AppVideoSearchVO;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.model.video.vo.Author;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitokservice.search.constant.VideoHotTitleCacheConstants;
import com.mwu.aitokservice.search.domain.VideoSearchHistory;
import com.mwu.aitokservice.search.service.VideoSearchHistoryService;
import com.mwu.aitokservice.search.service.VideoSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.mwu.aitokservice.search.constant.ESQueryConstants.*;

/**
 * VideoSearchServiceImpl
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/31
 **/
@Slf4j
@Service("videoSearchServiceImpl")
public class VideoSearchServiceImpl implements VideoSearchService {

    @Resource
    private ElasticsearchClient elasticsearchClient;

    @Resource
    private VideoSearchHistoryService videoSearchHistoryService;

    @Resource
    private RedisService redisService;



    @GrpcClient("aitok-video")
    private VideoServiceGrpc.VideoServiceBlockingStub videoServiceBlockingStub;

    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceBlockingStub;


    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 视频同步新增到ES
     */
    @Override
    public void videoSync(String videoId) {
        try {

            VideoIdRequest request = VideoIdRequest.newBuilder()
                    .setVideoId(Long.parseLong(videoId))
                    .build();
            VideoResponse response = videoServiceBlockingStub.apiGetVideoByVideoId(request);
            Video video = BeanCopyUtils.copyBean(response, Video.class);
            VideoSearchVO searchVO = convertToSearchVO(video);

            elasticsearchClient.index(i -> i
                    .index(ESIndexConstants.INDEX_VIDEO)
                    .id(searchVO.getVideoId())
                    .document(searchVO)
            );
        } catch (IOException e) {
            log.error("同步视频到ES失败, videoId: {}, 错误: {}", videoId, e.getMessage(), e);
        }
    }

    private VideoSearchVO convertToSearchVO(Video video) {
        VideoSearchVO searchVO = new VideoSearchVO();
        searchVO.setVideoId(String.valueOf(video.getId()));
        searchVO.setVideoTitle(video.getVideoTitle());
        searchVO.setPublishTime(Date.from(video.getCreateTime().atZone(ZoneId.systemDefault()).toInstant()));
        searchVO.setCoverImage(video.getCoverImage());
        searchVO.setVideoUrl(video.getVideoUrl());
        searchVO.setPublishType(video.getPublishType());
        searchVO.setUserId(video.getUserId());
        VideoIdRequest request = VideoIdRequest.newBuilder()
                .setVideoId(video.getId())
                .build();
        TagsIdListResponse response = videoServiceBlockingStub.apiGetVideoTagStack(request);
        List<Long> tagsIds = response.getTagIdsList();
        String[] tags = new String[tagsIds.size()];
        searchVO.setTags(response.getTagIdsList().stream()
                .map(String::valueOf)
                .toArray(String[]::new));
        // 获取标签名称
        searchVO.setTags(tags)  ;
        return searchVO;
    }

    @Autowired
    private ObjectMapper objectMapper;
    /**
     * 更新视频索引文档
     */
    @Override
    public void updateVideoDoc(String json) {
        try {
            VideoSearchVO videoSearchVO = objectMapper.readValue(json, VideoSearchVO.class);
            elasticsearchClient.update(u -> u
                            .index(ESIndexConstants.INDEX_VIDEO)
                            .id(videoSearchVO.getVideoId())
                            .doc(videoSearchVO)
                            .refresh(Refresh.True),
                    VideoSearchVO.class
            );
        } catch (IOException e) {
            log.error("更新ES文档失败, json: {}, 错误: {}", json, e.getMessage(), e);
        }
    }

    /**
     * 删除文档
     */
    @Override
    public void deleteVideoDoc(String videoId) {
        try {
            elasticsearchClient.deleteByQuery(d -> d
                    .index(ESIndexConstants.INDEX_VIDEO)
                    .query(q -> q
                            .term(t -> t
                                    .field("videoId")
                                    .value(videoId)
                            )
                    )
                    .refresh(true)
            );
        } catch (IOException e) {
            log.error("删除ES文档失败, videoId: {}, 错误: {}", videoId, e.getMessage(), e);
        }
    }

    /**
     * ES分页搜索视频
     */
    @Override
    public List<VideoSearchVO> searchVideoFromES(VideoSearchKeywordDTO dto) {
        if (StringUtils.isEmpty(dto.getKeyword())) {
            return Collections.emptyList();
        }

        // 保存搜索记录
        saveSearchHistory(dto);

        try {
            SearchResponse<VideoSearchVO> response = elasticsearchClient.search(buildVideoSearchRequest(dto), VideoSearchVO.class);

            return processSearchResponse(response);
        } catch (IOException e) {
            log.error("视频搜索失败, keyword: {}, 错误: {}", dto.getKeyword(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private void saveSearchHistory(VideoSearchKeywordDTO dto) {
        Long userId = UserContext.getUserId();
        if (StringUtils.isNotNull(userId) && dto.getFromIndex() == 0) {
            videoSearchHistoryService.insert(dto.getKeyword(), userId);
        }
    }

    private SearchRequest buildVideoSearchRequest(VideoSearchKeywordDTO dto) {
        // 处理发布时间限制
        Date minBehotTime = processPublishTimeLimit(dto);

        return SearchRequest.of(builder -> builder
                .index(ESIndexConstants.INDEX_VIDEO)
                .query(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .multiMatch(mm -> mm
                                                .query(dto.getKeyword())
                                                .fields(VideoSearchVO.VIDEO_TITLE, VideoSearchVO.TAGS)
                                        )
                                )
//                                .filter(f -> f
//                                        .range(r -> r
//                                                .field(VideoSearchVO.PUBLISH_TIME)
//                                                .gte(JsonData.of(minBehotTime))
//                                                .lte(JsonData.of(new Date()))
//                                        )
//                                )
                        )
                )
                .from((dto.getPageNum() - 1) * dto.getPageSize())
                .size(dto.getPageSize())
                .highlight(h -> h
                        .preTags(Highlight_preTags)
                        .postTags(Highlight_postTags)
                        .requireFieldMatch(false) //多字段时，需要设置为false
                        .fields(VideoSearchVO.VIDEO_TITLE, b -> b)
                        .fields(VideoSearchVO.TAGS, b -> b)
                ));
    }

    private Date processPublishTimeLimit(VideoSearchKeywordDTO dto) {
        if (StringUtils.isNotNull(dto.getPublishTimeLimit()) &&
                !dto.getPublishTimeLimit().equals(VideoSearchScreenPublishTime.NO_LIMIT.getCode())) {
            long dayStartLong = DateUtils.getTodayPlusStartLocalLong(-Objects.requireNonNull(VideoSearchScreenPublishTime.findByCode(dto.getPublishTimeLimit())).getLimit());
            return new Date(dayStartLong);
        }
        return dto.getMinBehotTime();
    }

    private List<VideoSearchVO> processSearchResponse(SearchResponse<VideoSearchVO> response) {
        return response.hits().hits().stream()
                .map(hit -> {
                    VideoSearchVO vo = hit.source();
                    applyHighlight(hit.highlight(), vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private void applyHighlight(Map<String, List<String>> highlights, VideoSearchVO vo) {
        if (highlights == null) return;

        // 处理标题高亮
        List<String> titleHighlights = highlights.get(VideoSearchVO.VIDEO_TITLE);
        if (titleHighlights != null && !titleHighlights.isEmpty()) {
            vo.setVideoTitle(String.join("", titleHighlights));
        }

        // 处理标签高亮
        List<String> tagsHighlights = highlights.get(VideoSearchVO.TAGS);
        if (tagsHighlights != null && !tagsHighlights.isEmpty()) {
            String[] tags = vo.getTags();
            for (int i = 0; i < tags.length; i++) {
                String originalTag = tags[i];
                for (String highlightedTag : tagsHighlights) {
                    String replacedTag = highlightedTag.replace(Highlight_preTags, "").replace(Highlight_postTags, "");
                    if (originalTag.equals(replacedTag)) {
                        tags[i] = highlightedTag;
                        break;
                    }
                }
            }
            vo.setTags(tags);
        }
    }

    /**
     * 搜索所有视频
     */
    @Override
    public List<VideoSearchVO> searchAllVideoFromES(VideoSearchKeywordDTO dto) {
        if (StringUtils.isNull(dto) || StringUtils.isBlank(dto.getKeyword())) {
            return Collections.emptyList();
        }

        try {
            SearchResponse<VideoSearchVO> response = elasticsearchClient.search(s -> s
                            .index(ESIndexConstants.INDEX_VIDEO)
                            .query(q -> q
                                    .bool(b -> b
                                            .must(m -> m
                                                    .fuzzy(f -> f
                                                            .field("videoTitle")
                                                            .value(dto.getKeyword())
                                                    )
                                            )
//                                            .filter(f -> f
//                                                    .range(r -> r
//                                                            .field("publishTime")
//                                                            .lt(JsonData.of(dto.getMinBehotTime() != null ? dto.getMinBehotTime() : new Date()))
//                                                    )
//                                            )
                                    )
                            )
                            .from((dto.getPageNum() - 1) * dto.getPageSize())
                            .size(dto.getPageSize())
                            .sort(so -> so
                                    .field(f -> f
                                            .field("publishTime")
                                            .order(SortOrder.Desc)
                                    )
                            )
                            .highlight(h -> h
                                    .fields("videoTitle", f -> f
                                            .preTags("<font class='keyword-hint'>")
                                            .postTags("</font>")
                                    )
                            ),
                    VideoSearchVO.class
            );

            return response.hits().hits().stream()
                    .map(hit -> {
                        VideoSearchVO vo = hit.source();
                        if (hit.highlight() != null) {
                            List<String> titleHighlights = hit.highlight().get("videoTitle");
                            if (titleHighlights != null && !titleHighlights.isEmpty()) {
                                vo.setVideoTitle(titleHighlights.get(0).replace("<font class='keyword-hint'>", "").replace("</font>", ""));
                            }
                        }
                        return vo;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("搜索所有视频失败, keyword: {}, 错误: {}", dto.getKeyword(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 从Redis中获取热搜排行榜
     */
    @Override
    public Set findSearchHot(PageDTO pageDTO) {
        int startIndex = (pageDTO.getPageNum() - 1) * pageDTO.getPageSize();
        int endIndex = startIndex + pageDTO.getPageSize() - 1;
        return redisService.getCacheZSetRange(VideoHotTitleCacheConstants.VIDEO_HOT_TITLE_PREFIX, startIndex, endIndex);
    }

    /**
     * 视频搜索推荐
     */
    @Override
    public List<String> pushVideoSearchSuggest(VideoSearchSuggestDTO videoSearchSuggestDTO) {
        if (StringUtils.isEmpty(videoSearchSuggestDTO.getKeyword())) {
            return Collections.emptyList();
        }

        try {
            // 使用IK分词器分析关键词
            AnalyzeResponse response = elasticsearchClient.indices().analyze(a -> a
                    .analyzer("ik_smart")
                    .text(videoSearchSuggestDTO.getKeyword())
            );

            Set<String> keywordRes = response.tokens().stream()
                    .map(t -> t.token())
                    .collect(Collectors.toSet());

            // MongoDB聚合查询
            Criteria criteria = Criteria.where("keyword").regex(String.join("|", keywordRes), "i");
            MatchOperation matchOperation = Aggregation.match(criteria);
            GroupOperation groupOperation = Aggregation.group("keyword").first("$$ROOT").as("doc");
            SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "doc.createdTime"));
            LimitOperation limitOperation = Aggregation.limit(10);
            SampleOperation sampleOperation = Aggregation.sample(10);

            Aggregation aggregation = Aggregation.newAggregation(
                    matchOperation, groupOperation, sortOperation, limitOperation, sampleOperation
            );

            return mongoTemplate.aggregate(aggregation, "video_search_history", VideoSearchHistory.class)
                    .getMappedResults()
                    .stream()
                    .map(VideoSearchHistory::getId)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("获取视频搜索推荐失败, keyword: {}, 错误: {}", videoSearchSuggestDTO.getKeyword(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * APP端视频搜索
     */
    @Override
    public PageData searchVideoFromESForApp(VideoSearchKeywordDTO dto) {
        if (StringUtils.isEmpty(dto.getKeyword())) {
            return PageData.emptyPage();
        }

        // 保存搜索记录
        saveAppSearchHistory(dto);

        try {
            SearchResponse<VideoSearchVO> response = elasticsearchClient.search(buildAppVideoSearchRequest(dto), VideoSearchVO.class);

            return processAppVideoSearchResponse(response);
        } catch (IOException e) {
            log.error("APP视频搜索失败, keyword: {}, 错误: {}", dto.getKeyword(), e.getMessage(), e);
            return PageData.emptyPage();
        }
    }

    private void saveAppSearchHistory(VideoSearchKeywordDTO dto) {
        Long userId = UserContext.getUserId();
        if (StringUtils.isNotNull(userId) && userId != 0L && dto.getFromIndex() == 0) {
            videoSearchHistoryService.insertPlatform(userId, dto.getKeyword(), VideoPlatformEnum.APP);
        }
    }

    private SearchRequest buildAppVideoSearchRequest(VideoSearchKeywordDTO dto) {
        Date minBehotTime = processPublishTimeLimit(dto);

        return SearchRequest.of(builder -> builder
                .index(ESIndexConstants.INDEX_VIDEO)
                .query(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .multiMatch(mm -> mm
                                                .query(dto.getKeyword())
                                                .fields(VideoSearchVO.VIDEO_TITLE, VideoSearchVO.TAGS)
                                        )
                                )
//                                .filter(f -> f
//                                        .range(r -> r
//                                                .field(VideoSearchVO.PUBLISH_TIME)
//                                                .gte(JsonData.of(minBehotTime))
//                                                .lte(JsonData.of(new Date()))
//                                        )
//                                )
                        )
                )
                .from((dto.getPageNum() - 1) * dto.getPageSize())
                .size(dto.getPageSize())
                .highlight(h -> h
                        .fields(VideoSearchVO.VIDEO_TITLE, f -> f
                                .preTags(Highlight_preTags_RED)
                                .postTags(Highlight_postTags_RED)
                        )
                        .fields(VideoSearchVO.TAGS, f -> f
                                .preTags(Highlight_preTags_RED)
                                .postTags(Highlight_postTags_RED)
                        )
                )
                .sort(s -> s
                        .score(sc -> sc.order(SortOrder.Desc))
                )
                .sort(s -> s
                        .field(f -> f
                                .field(VideoSearchVO.PUBLISH_TIME)
                                .order(SortOrder.Desc)
                        )
                ));
    }

    private PageData processAppVideoSearchResponse(SearchResponse<VideoSearchVO> response) {
        long total = response.hits().total().value();
        if (total == 0) {
            return PageData.emptyPage();
        }

        List<AppVideoSearchVO> result = response.hits().hits().stream()
                .map(hit -> {
                    VideoSearchVO source = hit.source();
                    AppVideoSearchVO vo = BeanCopyUtils.copyBean(source, AppVideoSearchVO.class);

                    // 处理高亮
                    applyAppHighlight(hit.highlight(), vo);

                    // 设置作者信息
                    GetByIdRequest request = GetByIdRequest.newBuilder().setUserId(source.getUserId()).build();
                    MemberResponse memberResponse = memberServiceBlockingStub.getById(request);
                    Member member = BeanCopyUtils.copyBean(memberResponse, Member.class);
                    vo.setAuthor(BeanCopyUtils.copyBean(member, Author.class));
                    vo.setCreateTime(DateUtils.date2LocalDateTime(source.getPublishTime()));
                    vo.setPublishTime(null);

                    // 设置观看数
                    Integer cacheViewNum = redisService.getCacheMapValue("video:view:num", source.getVideoId());
                    vo.setViewNum(StringUtils.isNull(cacheViewNum) ? 0L : cacheViewNum);

                    return vo;
                })
                .collect(Collectors.toList());

        return PageData.genPageData(result, total);
    }

    private void applyAppHighlight(Map<String, List<String>> highlights, AppVideoSearchVO vo) {
        if (highlights == null) return;

        // 处理标题高亮
        List<String> titleHighlights = highlights.get(VideoSearchVO.VIDEO_TITLE);
        if (titleHighlights != null && !titleHighlights.isEmpty()) {
            vo.setVideoTitle(String.join("", titleHighlights));
        }

        // 处理标签高亮
        List<String> tagsHighlights = highlights.get(VideoSearchVO.TAGS);
        if (tagsHighlights != null && !tagsHighlights.isEmpty()) {
            String[] tags = vo.getTags();
            for (int i = 0; i < tags.length; i++) {
                String originalTag = tags[i];
                for (String highlightedTag : tagsHighlights) {
                    String replacedTag = highlightedTag.replace(Highlight_preTags_RED, "").replace(Highlight_postTags_RED, "");
                    if (originalTag.equals(replacedTag)) {
                        tags[i] = highlightedTag;
                        break;
                    }
                }
            }
            vo.setTags(tags);
        }
    }
}
