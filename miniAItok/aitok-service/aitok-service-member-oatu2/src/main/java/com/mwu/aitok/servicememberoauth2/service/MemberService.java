package com.mwu.aitok.servicememberoauth2.service;

import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.member.domain.MemberInfo;
import com.mwu.aitok.model.member.dto.LoginUserDTO;
import com.mwu.aitok.model.member.dto.RegisterBody;
import com.mwu.aitok.model.member.dto.UpdatePasswordDTO;
import com.mwu.aitok.model.member.vo.MemberInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface MemberService {
    boolean register(RegisterBody registerBody);

    Map<String, String> login(LoginUserDTO loginUserDTO) throws Exception;

    Member updateUserInfo(Member user);

    MemberInfoVO getUserFromCache(Long userId);

    Boolean updatePass(UpdatePasswordDTO dto);

    String saveAvatar(MultipartFile file);

    Member getByMemberId(Long memberId);

    Member getByUserName(String userName);
}
