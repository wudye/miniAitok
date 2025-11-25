package com.mwu.aitok.service.video.service.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.model.video.domain.UserVideoCompilationRelation;
import com.mwu.aitok.model.video.dto.CompilationVideoPageDTO;
import com.mwu.aitok.model.video.vo.CompilationVideoVO;
import com.mwu.aitok.service.video.repository.UserVideoCompilationRelationRepository;
import com.mwu.aitok.service.video.service.IUserVideoCompilationRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IUserVideoCompilationRelationServiceImpl implements IUserVideoCompilationRelationService {

    private final UserVideoCompilationRelationRepository userVideoCompilationRelationRepository;

    /**
     * 将视频添加到合集
     *
     * @param videoId
     * @param compilationId
     * @return
     */
    @Transactional
    @Override
    public Boolean videoRelateCompilation(String videoId, Long compilationId) {
        UserVideoCompilationRelation userVideoCompilationRelation = new UserVideoCompilationRelation();
        userVideoCompilationRelation.setCompilationId(compilationId);
        userVideoCompilationRelation.setVideoId(videoId);
        userVideoCompilationRelationRepository.save(userVideoCompilationRelation);

        return true;
    }

    @Transactional
    @Override
    public boolean deleteRecordByVideoId(String videoId) {

        userVideoCompilationRelationRepository.deleteByVideoId(videoId);
        return true;
    }

    @Override
    public Page<UserVideoCompilationRelation> compilationVideoPage(CompilationVideoPageDTO pageDTO) {

        if (pageDTO == null || pageDTO.getCompilationId() == null) {
            return Page.empty();
        }
        String compilationId = pageDTO.getCompilationId().toString();

        Pageable pageable = PageRequest.of(pageDTO.getPageNum(), pageDTO.getPageSize());
        return userVideoCompilationRelationRepository.findByCompilationId(Long.valueOf(compilationId), pageable);

    }

    /**
     * 合集视频分页查询
     * TODO need to check this method, the frontend sends only id and numbers, the size is 0 or not
     * if size is 0,
     * @param pageDTO
     * @return
     */
    @Override
    public List<CompilationVideoVO> compilationVideoPageList(CompilationVideoPageDTO pageDTO) {


        if (pageDTO == null || pageDTO.getCompilationId() == null) {
            return List.of();
        }
        String compilationId = pageDTO.getCompilationId().toString();

       Pageable pageable = PageRequest.of(pageDTO.getPageNum(), pageDTO.getPageSize());
       Page<UserVideoCompilationRelation> page = userVideoCompilationRelationRepository.findByCompilationId(Long.valueOf(compilationId), pageable);

       return page.getContent().stream().map(item -> {
           CompilationVideoVO compilationVideoVO = new CompilationVideoVO();
           compilationVideoVO.setVideoId(item.getVideoId());
           compilationVideoVO.setCreateTime(item.getCreateTime());
           compilationVideoVO.setVideoTitle(item.getVideoId());
           compilationVideoVO.setVideoDesc(item.getVideoId());
           compilationVideoVO.setCoverImage(item.getVideoId());
           compilationVideoVO.setVideoUrl(item.getVideoId());
           return compilationVideoVO;
       }).toList();


    }

    @Override
    public Long compilationVideoPageCount(Long compilationId) {
        return userVideoCompilationRelationRepository.countByCompilationId(compilationId);
    }

    @Override
    public Long getCompilationIdByVideoId(String videoId) {
        List<UserVideoCompilationRelation> list = userVideoCompilationRelationRepository.findByVideoId(videoId);
        if (list != null && !list.isEmpty()) {
            return list.get(0).getCompilationId();
        }
        return 0L;
    }
}
