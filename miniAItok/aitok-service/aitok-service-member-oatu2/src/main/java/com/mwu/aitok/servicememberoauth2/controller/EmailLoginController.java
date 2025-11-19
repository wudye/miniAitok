package com.mwu.aitok.servicememberoauth2.controller;

import com.mwu.aitiokcoomon.core.constant.Constants;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitok.model.member.dto.SmsLoginDTO;
import com.mwu.aitok.model.member.enums.LoginTypeEnum;
import com.mwu.aitok.servicememberoauth2.service.LoginService;
import com.mwu.aitokstarter.email.SmsCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class EmailLoginController {
    @Resource
    private LoginService loginService;
    @GetMapping("/loginAuthCode/{email}")
    public R<String> sendCode(@PathVariable("email") String email) {
        SmsCode smsCode = loginService.sendLoginAuthCode(email);
        return R.ok(smsCode.getCode());
    }


    @PostMapping("/sms-login")
    public R<Map<String, String>> smsLogin(@Validated @RequestBody SmsLoginDTO smsLoginDTO) {
//        String token = loginService.smsLogin(smsLoginDTO);
        Map<String, String> map = loginService.loginEmail(smsLoginDTO);

        return R.ok(map);
    }
}
