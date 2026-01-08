package com.mwu.aitok.service.video.service.impl;

import com.mwu.aitok.model.video.domain.VideoImage;
import com.mwu.aitok.service.video.repository.VideoImageRepository;
import com.mwu.aitok.service.video.service.IVideoImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IVideoImageServiceImpl implements IVideoImageService {


    private final VideoImageRepository videoImageRepository;
    /**
     * 通过视频id查询视频图片
     *
     * @param videoId
     * @return
     */
    @Override
    public List<VideoImage> queryImagesByVideoId(String videoId) {

        return videoImageRepository.findByVideoId(videoId);
    }

    @Override
    public boolean deleteVideoImagesByVideoId(String videoId) {

        videoImageRepository.deleteByVideoId(videoId);


        return true;
    }

    @Override
    public void save(VideoImage videoImage) {
        videoImageRepository.save(videoImage);
    }
}
