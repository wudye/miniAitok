package com.mwu.aitok.service.video.controller.v1;


import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.domain.vo.PageData;
import com.mwu.aitok.model.video.domain.UserVideoCompilation;
import com.mwu.aitok.model.video.dto.CompilationVideoPageDTO;
import com.mwu.aitok.model.video.dto.UpdateUserVideoCompilationDTO;
import com.mwu.aitok.model.video.dto.UserVideoCompilationPageDTO;
import com.mwu.aitok.service.video.repository.UserVideoCompilationRepository;
import com.mwu.aitok.service.video.service.IUserVideoCompilationService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户视频合集表(UserVideoCompilation)表控制层
 *
 * @author mwu
 * @since 2023-11-27 18:08:37
 */
@RestController
@RequestMapping("/api/v1/userVideoCompilation")
public class UserVideoCompilationController {

    @Resource
    private IUserVideoCompilationService userVideoCompilationService;

    @Autowired
    private UserVideoCompilationRepository userVideoCompilationRepository;

    /**
     * 创建合集
     */
    @PostMapping()
    public R<Boolean> createVideoCompilation(@RequestBody UserVideoCompilation userVideoCompilation) {
        userVideoCompilation.setUserId(UserContext.getUserId());
        userVideoCompilation.setCreateTime(LocalDateTime.now());
        userVideoCompilationRepository.save(userVideoCompilation);
        return R.ok(true    );
    }

    /**
     * 更新合集
     */
    @PutMapping("/update")
    public R<Boolean> createVideoCompilation(@RequestBody UpdateUserVideoCompilationDTO updateUserVideoCompilationDTO) {
        return R.ok(userVideoCompilationService.updateVideoCompilationInfo(updateUserVideoCompilationDTO));
    }

    /**
     * 分页我的合集
     */
    @PostMapping("/mp")
    public PageData videoCompilationMyPage(@RequestBody UserVideoCompilationPageDTO pageDTO) {
        return (PageData) userVideoCompilationService.videoCompilationMyPage(pageDTO);
    }

    /**
     * 分页用户合集
     */
    @PostMapping("/up")
    public PageData videoCompilationUserPage(@RequestBody UserVideoCompilationPageDTO pageDTO) {
        return (PageData) userVideoCompilationService.videoCompilationUserPage(pageDTO);
    }

    /**
     * 分页合集视频
     */
    @PostMapping("/videoPage")
    public PageData compilationVideoPage(@RequestBody CompilationVideoPageDTO pageDTO) {
        return (PageData) userVideoCompilationService.compilationVideoPage(pageDTO);
    }

}

