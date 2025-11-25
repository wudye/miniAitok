package com.mwu.atiokservice.behave.service.impl;


import com.mwu.aitok.model.behave.domain.UserVideoBehave;
import com.mwu.aitok.model.behave.enums.UserVideoBehaveEnum;
import com.mwu.atiokservice.behave.repository.UserVideoBehaveRepository;
import com.mwu.atiokservice.behave.service.IUserVideoBehaveService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 用户视频行为表(UserVideoBehave)表服务实现类
 *
 * @author roydon
 * @since 2024-04-19 14:21:15
 */
@Service("userVideoBehaveService")
public class UserVideoBehaveServiceImpl implements IUserVideoBehaveService {
    @Resource
    private UserVideoBehaveRepository userVideoBehaveMapper;

    /**
     * 同步用户行为到表 UserVideoBehave
     *
     * @param userId
     * @param videoId
     * @param behave
     * @return
     */
    @Async
    @Override
    public Boolean syncUserVideoBehave(Long userId, String videoId, UserVideoBehaveEnum behave) {
        UserVideoBehave userVideoBehave = new UserVideoBehave();
        userVideoBehave.setUserId(userId);
        userVideoBehave.setUserBehave(behave.getCode());
        userVideoBehave.setVideoId(videoId);
        userVideoBehaveMapper.save(userVideoBehave);
        return true;
    }
}
