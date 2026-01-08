package com.mwu.aitokservice.recommend.service.impl;

import com.mwu.aitokservice.recommend.repository.UserVideoBehaveRepository;
import com.mwu.aitokservice.recommend.service.IUserVideoBehaveService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 用户视频行为表(UserVideoBehave)表服务实现类
 *
 * @author mwu
 * @since 2024-04-27 18:56:49
 */
@Service("userVideoBehaveService")
public class UserVideoBehaveServiceImpl  implements IUserVideoBehaveService {

    @Resource
    private UserVideoBehaveRepository userVideoBehaveMapper;

}
