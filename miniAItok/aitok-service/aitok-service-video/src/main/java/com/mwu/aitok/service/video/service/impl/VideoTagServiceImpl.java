package com.mwu.aitok.service.video.service.impl;

import com.mwu.aitok.model.video.domain.VideoTag;
import com.mwu.aitok.service.video.repository.VideoTagRepository;
import com.mwu.aitok.service.video.service.VideoTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoTagServiceImpl implements VideoTagService {

    private final VideoTagRepository videoTagRepository;
    @Override
    public List<VideoTag> random10VideoTags() {
        Pageable pageable= PageRequest.of(0,10);
        Page<VideoTag> videoTagPage= videoTagRepository.findAll(pageable);
        List<VideoTag> videoTags= videoTagPage.getContent();
        return videoTags;


    }
}
