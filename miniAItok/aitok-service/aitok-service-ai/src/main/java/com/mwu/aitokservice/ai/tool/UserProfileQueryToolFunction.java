package com.mwu.aitokservice.ai.tool;

import com.fasterxml.jackson.annotation.JsonClassDescription;

import com.mwu.aitiokcoomon.core.utils.bean.BeanUtils;
import com.mwu.aitok.GetByIdRequest;
import com.mwu.aitok.GrpcMemberService;
import com.mwu.aitok.MemberResponse;
import com.mwu.aitok.MemberServiceGrpc;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitokcommon.ai.util.AiUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

/**
 * 工具：当前用户信息查询
 * <p>
 * 同时，也是展示 ToolContext 上下文的使用
 *
 * @author Ren
 */
@Component("user_profile_query")
public class UserProfileQueryToolFunction implements BiFunction<UserProfileQueryToolFunction.Request, ToolContext, UserProfileQueryToolFunction.Response> {



    @GrpcClient("aitok-member")
    private MemberServiceGrpc.MemberServiceBlockingStub memberServiceGrpc;

    @Data
    @JsonClassDescription("当前用户信息查询")
    public static class Request {
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {

        /**
         * 用户ID
         */
        private Long id;
        /**
         * 用户昵称
         */
        private String nickName;

        /**
         * 手机号码
         */
        private String telephone;
        /**
         * 用户头像
         */
        private String avatar;

    }

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        Long loginUserId = (Long) toolContext.getContext().get(AiUtils.TOOL_CONTEXT_LOGIN_USER);
        if (loginUserId == null) {
            return null;
        }

        GetByIdRequest getByIdRequest = GetByIdRequest.newBuilder().setUserId(loginUserId).build();
        MemberResponse memberResponse = memberServiceGrpc.getById(getByIdRequest);

        Member member = BeanUtils.toBean(memberResponse, Member.class);
        return BeanUtils.toBean(member, Response.class);

    }

}
