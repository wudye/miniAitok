package com.mwu.aitok.servicememberoauth2.service;

import com.mwu.aitok.model.member.dto.SmsLoginDTO;
import com.mwu.aitok.model.member.enums.LoginTypeEnum;
import com.mwu.aitokstarter.email.SmsCode;

import java.util.Map;
import java.util.Optional;

public interface LoginService {



    Map<String, String> loginEmail(SmsLoginDTO smsLoginDTO) ;

    SmsCode sendLoginAuthCode(String email);
}
