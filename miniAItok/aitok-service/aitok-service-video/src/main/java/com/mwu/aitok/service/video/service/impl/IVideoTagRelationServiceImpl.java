package com.mwu.aitok.service.video.service.impl;

import com.mwu.aitok.model.video.domain.VideoTag;
import com.mwu.aitok.model.video.domain.VideoTagRelation;
import com.mwu.aitok.service.video.repository.VideoTagRelationRepository;
import com.mwu.aitok.service.video.repository.VideoTagRepository;
import com.mwu.aitok.service.video.service.IVideoTagRelationService;
import com.mwu.aitok.service.video.service.VideoTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("videoTagRelationService")

@RequiredArgsConstructor
public class IVideoTagRelationServiceImpl implements IVideoTagRelationService {

    private final VideoTagRelationRepository videoTagRelationRepository;
    private final VideoTagRepository videoTagService;

    /**
     * 根据视频id和标签id数组批量插入，标签最大不超过5个，不用考虑性能问题
     *
     * @param videoId
     * @param tagIds
     */
    @Transactional
    @Override
    public boolean saveVideoTagRelationBatch(String videoId, Long[] tagIds) {
        List<VideoTagRelation> list = new ArrayList<>();
        for (Long tagId : tagIds) {
            VideoTagRelation videoTagRelation = new VideoTagRelation();
            videoTagRelation.setVideoId(videoId);
            videoTagRelation.setTagId(tagId);
            list.add(videoTagRelation);
            videoTagRelationRepository.save(videoTagRelation);
        }
        return true;
    }

    @Override
    public String[] queryVideoTags(String videoId) {


        return queryVideoTagsReturnList(videoId).toArray(new String[0]);
    }

    @Override
    public List<String> queryVideoTagsReturnList(String videoId) {
        List<VideoTagRelation> videoTagRelationList = videoTagRelationRepository.findByVideoId(videoId);
        List<Long> tagIds = new ArrayList<>();
        for (VideoTagRelation videoTagRelation : videoTagRelationList) {
            tagIds.add(videoTagRelation.getTagId());
        }
        List<VideoTag> videoTagList = videoTagService.findAllById(tagIds);
        List<String> tagNames = new ArrayList<>();
        for (VideoTag videoTag : videoTagList) {
            tagNames.add(videoTag.getTag());
        }
        return tagNames;

    }

    @Override
    public List<VideoTag> queryVideoTagsByVideoId(String videoId) {
        List<VideoTagRelation> videoTagRelationList = videoTagRelationRepository.findByVideoId(videoId);

        List<VideoTag> videoTagList = new ArrayList<>();
        for (VideoTagRelation videoTagRelation : videoTagRelationList) {
            VideoTag videoTag = videoTagService.findById(videoTagRelation.getTagId()).orElse(null);
            videoTagList.add(videoTag);
        }
        return videoTagList;
    }

    @Override
    public List<Long> queryVideoTagIdsByVideoId(String videoId) {

        List<VideoTagRelation> videoTagRelationList = videoTagRelationRepository.findByVideoId(videoId);
        List<Long> tagIds = new ArrayList<>();
        for (VideoTagRelation videoTagRelation : videoTagRelationList) {
            tagIds.add(videoTagRelation.getTagId());
        }
        return tagIds;
    }


    @Override
    public boolean deleteRecordByVideoId(String videoId) {

        videoTagRelationRepository.deleteByVideoId(videoId);
        return true;
    }
}
