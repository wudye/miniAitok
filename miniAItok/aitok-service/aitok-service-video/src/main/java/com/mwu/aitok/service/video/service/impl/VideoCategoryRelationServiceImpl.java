package com.mwu.aitok.service.video.service.impl;

import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.model.video.domain.VideoCategoryRelation;
import com.mwu.aitok.service.video.repository.VideoCategoryRelationRepository;
import com.mwu.aitok.service.video.service.IVideoCategoryRelationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频分类关联表(VideoCategoryRelation)表服务实现类
 *
 * @author mwu
 * @since 2023-10-31 14:44:35
 */
@Service
public class VideoCategoryRelationServiceImpl implements IVideoCategoryRelationService {
    @Resource
    private VideoCategoryRelationRepository videoCategoryRelationMapper;

    @Override
    public boolean saveVideoCategoryRelation(VideoCategoryRelation videoCategoryRelation) {
        try {
            videoCategoryRelationMapper.save(videoCategoryRelation);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 根据视频id查询分类ids
     *
     * @param videoId
     * @return
     */
    @Override
    public List<Long> queryVideoCategoryIdsByVideoId(String videoId) {

        if (StringUtils.isNull(videoId)) {
            return Collections.emptyList();
        }

        List<VideoCategoryRelation> videoCategoryRelationList = videoCategoryRelationMapper.findByVideoId(videoId);
        return videoCategoryRelationList.stream().map(VideoCategoryRelation::getCategoryId).collect(Collectors.toList());

    }

    /**
     * 删除视频分类关联
     *
     * @param videoId
     * @return
     */
    @Override
    public boolean deleteRecordByVideoId(String videoId) {

        videoCategoryRelationMapper.deleteByVideoId(videoId);
        return true;


    }
}
