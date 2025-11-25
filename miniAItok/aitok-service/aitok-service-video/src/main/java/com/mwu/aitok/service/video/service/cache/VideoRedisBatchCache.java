package com.mwu.aitok.service.video.service.cache;

import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitok.service.video.service.IVideoService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * VideoRedisBatchCache
 *
 * @AUTHOR: roydon
 * @DATE: 2024/4/8
 **/
@Service
public class VideoRedisBatchCache extends AbstractRedisStringCache<String, Video> {

    @Resource
    @Lazy
    private IVideoService videoService;

    @Override
    protected String getKey(String videoId) {
        return "video:video_batch:" + videoId;
    }

    @Override
    protected Long getExpireSeconds() {
        return 24 * 60 * 60L;// 24小时
    }

    @Override
    protected Map<String, Video> load(List<String> videoIds) {
        List<Video> videoList = videoService.listByIds(videoIds);
        return videoList.stream()
                .collect(Collectors.toMap(video -> String.valueOf(video.getId()), Function.identity()));    }
}