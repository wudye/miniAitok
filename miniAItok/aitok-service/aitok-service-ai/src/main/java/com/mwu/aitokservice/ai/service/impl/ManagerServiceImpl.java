package com.mwu.aitokservice.ai.service.impl;


import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitiokcoomon.core.utils.JwtUtil;
import com.mwu.aitiokcoomon.core.utils.bean.BeanCopyUtils;
import com.mwu.aitok.GetByIdRequest;
import com.mwu.aitok.GetByUserNameRequest;
import com.mwu.aitok.MemberResponse;
import com.mwu.aitok.MemberServiceGrpc;
import com.mwu.aitok.model.ai.AiManagerDO;
import com.mwu.aitok.model.ai.AiManagerUserInfo;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitokservice.ai.controller.admin.AdminManagerController;
import com.mwu.aitokservice.ai.mapper.ManagerMapper;
import com.mwu.aitokservice.ai.service.IManagerService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Objects;
import java.util.Optional;

import static com.mwu.aitok.model.common.enums.HttpCodeEnum.PASSWORD_ERROR;
import static com.mwu.aitok.model.common.enums.HttpCodeEnum.USER_NOT_EXISTS;


/**
 * AI管理员表(AiManager)表服务实现类
 *
 * @author roydon
 * @since 2025-05-30 23:39:17
 */
@Service
public class ManagerServiceImpl implements IManagerService {



    @Autowired
    private ManagerMapper aiManagerMapper;
    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceBlockingStub;

    @Override
    public String login(AdminManagerController.LoginDTO dto) {
        

        GetByUserNameRequest request = GetByUserNameRequest.newBuilder().setUserName(dto.username()).build();
        MemberResponse response = memberServiceBlockingStub.getByUserName(request);
        Member member = BeanCopyUtils.copyBean(response, Member.class);
        if (Objects.isNull(member)) {
            throw new CustomException(USER_NOT_EXISTS);
        }
        // 比对密码
        String salt = member.getSalt();
        String pswd = dto.password();
        pswd = DigestUtils.md5DigestAsHex((pswd + salt).getBytes());
        if (!pswd.equals(member.getPassword())) {
            throw new CustomException(PASSWORD_ERROR);
        }
        // ai管理员表是否存在该用户

        Optional<AiManagerDO> aiManagerDO = aiManagerMapper.findByUserId(member.getUserId());
        if (aiManagerDO.isEmpty()) {
            throw new CustomException(USER_NOT_EXISTS);
        }

        return JwtUtil.getToken(member.getUserId());
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @Override
    public AiManagerUserInfo userInfo() {
        // todo 用token的userId为主键 查询管理员，校验状态

        // todo 查询用户信息
        GetByIdRequest getByIdRequest = GetByIdRequest.newBuilder().setUserId(UserContext.getUserId()).build();
        MemberResponse response = memberServiceBlockingStub.getById(getByIdRequest);
        Member member = BeanCopyUtils.copyBean(response, Member.class);
        return BeanCopyUtils.copyBean( member, AiManagerUserInfo.class);
    }
}
