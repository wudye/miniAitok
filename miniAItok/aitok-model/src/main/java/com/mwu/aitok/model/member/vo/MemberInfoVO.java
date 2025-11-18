package com.mwu.aitok.model.member.vo;



import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.member.domain.MemberInfo;
import com.mwu.aitok.model.member.vo.app.AppMemberInfoVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * MemberInfoVO
 *
 * @AUTHOR: mwu
 * @DATE: 2023/11/12
 * 用户详情返回体
 **/
@NoArgsConstructor
@Data
public class MemberInfoVO  {

    private Member member;
    // 用户详情
    private MemberInfo memberInfo;

    public MemberInfoVO(Member member) {
        this.member = member;
    }
    public static MemberInfoVO of(Member member) {
        return new MemberInfoVO(member);
    }

}
