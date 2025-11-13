package com.mwu.model.member.vo.app;

import com.mwu.model.member.domain.Member;
import com.mwu.model.member.domain.MemberInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AppMemberInfoVO
 *
 * @author mwu
 * @since  2025/11/13
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class AppMemberInfoVO extends Member {

    private MemberInfo memberInfo;

    private Boolean weatherFollow;

    private Long likeCount;

    private Long followCount;


    private Long fansCount;

}
