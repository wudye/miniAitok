package com.mwu.model.member.vo;

import com.mwu.model.member.domain.Member;
import com.mwu.model.member.domain.MemberInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MemberInfoVO View Object
 *
 * @author mwu
 * @since  2025/11/13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MemberInfoVO extends Member {

    // 用户详情
    private MemberInfo memberInfo;

}
