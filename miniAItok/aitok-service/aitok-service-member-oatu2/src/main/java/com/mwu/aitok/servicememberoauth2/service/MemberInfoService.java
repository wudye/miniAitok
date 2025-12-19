package com.mwu.aitok.servicememberoauth2.service;

import com.mwu.aitok.model.member.domain.MemberInfo;
import org.springframework.web.multipart.MultipartFile;

public interface MemberInfoService {
    String uploadBackImage(MultipartFile file);

    Boolean saveOrUpdate(MemberInfo memberInfo);
}
