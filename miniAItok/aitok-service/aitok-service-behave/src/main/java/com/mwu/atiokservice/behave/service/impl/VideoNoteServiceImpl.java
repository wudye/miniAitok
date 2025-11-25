package com.mwu.atiokservice.behave.service.impl;


import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitok.GetByIdRequest;
import com.mwu.aitok.MemberResponse;
import com.mwu.aitok.MemberServiceGrpc;
import com.mwu.aitok.model.behave.domain.VideoNote;
import com.mwu.aitok.model.behave.dto.VideoNotePageDTO;
import com.mwu.aitok.model.behave.enums.VideoNoteDelFlagEnum;
import com.mwu.aitok.model.behave.vo.VideoNoteVO;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.video.vo.Author;
import com.mwu.atiokservice.behave.repository.VideoNoteRepository;
import com.mwu.atiokservice.behave.service.IVideoNoteService;
import jakarta.annotation.Resource;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.member;

/**
 * 视频笔记表(VideoNote)表服务实现类
 *
 * @author roydon
 * @since 2024-05-05 18:51:05
 */
@Service("videoNoteService")
public class VideoNoteServiceImpl  implements IVideoNoteService {

    @Resource
    private VideoNoteRepository videoNoteMapper;


    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceBlockingStub;

    /**
     * 分页
     *
     * @param pageDTO
     * @return
     */
    @Override
    public PageData<VideoNoteVO> queryVideoNotePage(VideoNotePageDTO pageDTO) {


        String videoId = pageDTO.getVideoId();
        if (Objects.isNull(videoId) || videoId.isEmpty()) {
            return PageData.emptyPage();
        }
        Pageable pageable = PageRequest.of(pageDTO.getPageNum() - 1, pageDTO.getPageSize(), Sort.Direction.DESC, "create_time");

        Page<VideoNote> page = videoNoteMapper.findAllByVideoIdAndDelFlag(videoId, VideoNoteDelFlagEnum.NORMAL.getCode(), pageable);
        List<VideoNote> records = page.getContent();
        if (CollectionUtils.isEmpty(records)) {
            return PageData.emptyPage();
        }
        List<VideoNoteVO> voList = BeanCopyUtils.copyBeanList(records, VideoNoteVO.class);
        voList.forEach(v -> {
            v.setNoteContent(v.getNoteContent().substring(0, Math.min(v.getNoteContent().length(), 200)));

            GetByIdRequest getByIdRequest = GetByIdRequest.newBuilder().setUserId(v.getUserId()).build();
            MemberResponse memberResponse = memberServiceBlockingStub.getById(getByIdRequest);

            if (!Objects.isNull(memberResponse)) {
                Author author = BeanCopyUtils.copyBean(member, Author.class);
                v.setAuthor(author);
            }
        });
        return PageData.genPageData(voList, page.getTotalElements());
    }

}
