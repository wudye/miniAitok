package com.mwu.aitok.service.video.service.impl;

import com.mwu.aitok.service.video.repository.VideoSensitiveRepository;
import com.mwu.aitok.service.video.service.VideoSensitiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoSensitiveServiceImpl implements VideoSensitiveService {

    @Autowired
    private  VideoSensitiveRepository videoSensitiveRepository;
}
