package com.mwu.aitok.service.video.service.impl;

import com.mwu.aitok.model.video.domain.VideoPosition;
import com.mwu.aitok.service.video.repository.VideoPositionRepository;
import com.mwu.aitok.service.video.service.IVideoPositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("videoPositionService")
@RequiredArgsConstructor
public class IVideoPositionServiceImpl implements IVideoPositionService {

    private final VideoPositionRepository videoPositionRepository;
    @Override
    public VideoPosition queryPositionByVideoId(String videoId) {
        if (videoId == null) {
            return null;
        }
        return videoPositionRepository.findById(videoId).orElse(null);
    }

    @Override
    public boolean deleteRecordByVideoId(String videoId) {
        if (videoId == null) {
            return true;
        }
        videoPositionRepository.deleteByVideoId(videoId);


        return true;
    }

    @Override
    public VideoPosition save(VideoPosition videoPosition) {
        videoPosition = videoPositionRepository.save(videoPosition);
        return videoPosition;
    }
}
