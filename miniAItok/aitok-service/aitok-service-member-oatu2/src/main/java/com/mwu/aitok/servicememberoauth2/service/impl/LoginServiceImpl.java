package com.mwu.aitok.servicememberoauth2.service.impl;

import com.mwu.aitiokcoomon.core.utils.EmailUtils;
import com.mwu.aitiokcoomon.core.utils.IpUtils;
import com.mwu.aitiokcoomon.core.utils.ServletUtils;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.member.dto.SmsLoginDTO;
import com.mwu.aitok.model.member.enums.LoginTypeEnum;
import com.mwu.aitok.servicememberoauth2.constants.UserCacheConstants;
import com.mwu.aitok.servicememberoauth2.entity.TokenPair;
import com.mwu.aitok.servicememberoauth2.repository.MemberRepository;
import com.mwu.aitok.servicememberoauth2.security.JwtService;
import com.mwu.aitok.servicememberoauth2.service.LoginService;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitokstarter.email.GmailMailService;
import com.mwu.aitokstarter.email.SmsCode;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.mwu.aitok.servicememberoauth2.constants.SmsConstant.SMS_LOGIN_AUTH_CODE_EXPIRE_TIME;
import static com.mwu.aitok.servicememberoauth2.constants.SmsConstant.SMS_LOGIN_AUTH_CODE_KEY;

@Service
public class LoginServiceImpl implements LoginService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private GmailMailService emailService;

    @Autowired
    private  MemberRepository memberRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisService redisService;

    @Override
    public Map<String, String> loginEmail(SmsLoginDTO smsLoginDTO) {

        String email = smsLoginDTO.getEmail();
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        if(smsLoginDTO.getSmsCode().equals(redisTemplate.opsForValue().get(SMS_LOGIN_AUTH_CODE_KEY + email))){
            redisTemplate.delete(SMS_LOGIN_AUTH_CODE_KEY + email);
            Map<String, String> tokenMap = new HashMap<>();
            Member member1 = member.get();
            member1.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
            member1.setLoginLocation(IpUtils.getIpLocation(ServletUtils.getRequest()));
            try  {
                TokenPair token = jwtService.createAndStoreTokens(member1.getUserName(), String.valueOf(member1.getUserId()));
                System.out.println("token: " + token.accessToken());
                System.out.println("token: " + token.refreshToken());


                tokenMap.put("access_token", token.accessToken());
                tokenMap.put("refresh_token", token.refreshToken());
                memberRepository.save(member1);

                Long userId = member1.getUserId();
                redisService.deleteObject(UserCacheConstants.USER_INFO_PREFIX + userId);

                redisService.setCacheObject(UserCacheConstants.USER_INFO_PREFIX +userId, member1);
                redisService.expire(UserCacheConstants.USER_INFO_PREFIX + userId, UserCacheConstants.USER_INFO_EXPIRE_TIME, TimeUnit.SECONDS);


            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return tokenMap;

        }else{
            throw new RuntimeException("验证码错误");
        }

    }

    @Override
    public SmsCode sendLoginAuthCode(String email) {
        if (!EmailUtils.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address");
        }
        String code = SMS_LOGIN_AUTH_CODE_KEY + email;

        String authCOde = redisTemplate.opsForValue().get(code);

        if (authCOde != null) {
            throw new RuntimeException("请勿重复发送-" + email);
        }
        String generateCode = RandomStringUtils.randomNumeric(6);

        emailService.sendSimpleEmail(email, "login verify", "login code：" + generateCode, "aitok.com");

        redisTemplate.opsForValue().set(code, generateCode, SMS_LOGIN_AUTH_CODE_EXPIRE_TIME, TimeUnit.MINUTES);

        return new SmsCode(generateCode, SMS_LOGIN_AUTH_CODE_EXPIRE_TIME);
    }
}
