package com.mwu.aitok.servicememberoauth2.controller;

import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.member.domain.MemberInfo;
import com.mwu.aitok.servicememberoauth2.service.MemberInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/info")
public class MemberInfoController {

    @Autowired
    private MemberInfoService memberInfoService;
    @PostMapping("/backImage/upload")
    public R<String> uploadBackImage(@RequestParam("file") MultipartFile file) {
        return R.ok(memberInfoService.uploadBackImage(file));
    }

    @PutMapping("/update")
    public R<Boolean> updateMemberInfo(@RequestBody MemberInfo memberInfo) {
        return R.ok(memberInfoService.saveOrUpdate(memberInfo));
    }
}
