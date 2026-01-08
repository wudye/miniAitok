package com.mwu.atiokservice.behave.controller.v1;


import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.behave.domain.VideoNote;
import com.mwu.aitok.model.behave.dto.VideoNotePageDTO;
import com.mwu.aitok.model.behave.vo.VideoNoteVO;
import com.mwu.atiokservice.behave.repository.VideoNoteRepository;
import com.mwu.atiokservice.behave.service.IVideoNoteService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 视频笔记表(VideoNote)表控制层
 *
 * @author mwu
 * @since 2024-05-05 18:51:04
 */
@RestController
@RequestMapping("/api/v1/videoNote")
public class VideoNoteController {

    @Resource
    private IVideoNoteService videoNoteService;

    @Autowired
    private VideoNoteRepository videoNoteRepository;

    /**
     * 分页查询
     */
    @PostMapping("/page")
    public PageData<VideoNoteVO> queryVideoNotePage(@Validated @RequestBody VideoNotePageDTO pageDTO) {
        return this.videoNoteService.queryVideoNotePage(pageDTO);
    }

    /**
     * 通过主键查询单条数据
     */
    @GetMapping("/{noteId}")
    public R<?> queryById(@PathVariable("noteId") Long noteId) {


        return R.ok(this.videoNoteRepository.findById(noteId).orElse(null));
    }

    /**
     * 新增数据
     */
    @PostMapping
    public R<?> add(@Validated @RequestBody VideoNote videoNote) {
        videoNote.setUserId(UserContext.getUserId());
        videoNote.setCreateTime(LocalDateTime.now());
        videoNote.setCreateBy(UserContext.getUserId());
        this.videoNoteRepository.save(videoNote);
        return R.ok("success");
    }

    /**
     * 编辑数据
     */
    @PutMapping
    public R<?> edit(@Validated @RequestBody VideoNote videoNote) {

        VideoNote videoNote1 = this.videoNoteRepository.findById(videoNote.getNoteId()).orElse(null);
        if (Objects.isNull(videoNote1)) {
            return R.fail("笔记不存在");
        }
        if (!videoNote1.getUserId().equals(UserContext.getUserId())) {
            return R.fail("无权限编辑该笔记");
        }
        videoNote1.setNoteId(videoNote.getNoteId());
        videoNote1.setNoteContent(videoNote.getNoteContent());
        videoNote1.setNoteTitle(videoNote.getNoteTitle());
        videoNote1.setUpdateTime(LocalDateTime.now());
        videoNote1.setUpdateBy(UserContext.getUserId());

        videoNote1.setUpdateTime(LocalDateTime.now());
        videoNote1.setUpdateBy(UserContext.getUserId());
        this.videoNoteRepository.save(videoNote);
        return R.ok("success");    }

    /**
     * 删除数据
     */
    @DeleteMapping("/{noteId}")
    public R<?> removeById(@PathVariable("noteId") Long noteId) {

        videoNoteRepository.deleteById(noteId);
        return R.ok("success");
    }

}

