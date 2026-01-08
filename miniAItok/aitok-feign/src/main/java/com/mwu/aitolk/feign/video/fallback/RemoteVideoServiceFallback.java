package com.mwu.aitolk.feign.video.fallback;


import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.video.domain.Video;
import com.mwu.aitolk.feign.video.RemoteVideoService;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RemoteVideoServiceFallback
 *
 * @AUTHOR: mwu
 * @DATE: 2023/10/28
 **/
@Component
public class RemoteVideoServiceFallback implements FallbackFactory<RemoteVideoService> {

    /**
     * @param throwable
     * @return
     */
    @Override
    public RemoteVideoService create(Throwable throwable) {
        return new RemoteVideoService() {
            @Override
            public R<List<Video>> queryVideoByVideoIds(List<String> videoIds) {
                return R.fail(null);
            }

        };
    }
}
