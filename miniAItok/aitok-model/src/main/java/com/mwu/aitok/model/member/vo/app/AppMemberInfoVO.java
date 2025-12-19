package com.mwu.aitok.model.member.vo.app;



import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.member.domain.MemberInfo;
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
@Data
@NoArgsConstructor
public class AppMemberInfoVO  {

    private Member member;
    // 用户详情
    private MemberInfo memberInfo;

    // 是否关注
    private Boolean weatherFollow;

    // 获赞
    private Long likeCount;

    // 关注
    private Long followCount;

    // 粉丝
    private Long fansCount;

    public AppMemberInfoVO(Member member) {
        this.member = member;
    }
    public static AppMemberInfoVO of(Member member) {
        return new AppMemberInfoVO(member);
    }

}
