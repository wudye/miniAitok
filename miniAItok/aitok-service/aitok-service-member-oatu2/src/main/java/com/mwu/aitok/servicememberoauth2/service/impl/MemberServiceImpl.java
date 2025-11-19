package com.mwu.aitok.servicememberoauth2.service.impl;

import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitiokcoomon.core.utils.IdUtils;
import com.mwu.aitiokcoomon.core.utils.IpUtils;
import com.mwu.aitiokcoomon.core.utils.ServletUtils;
import com.mwu.aitiokcoomon.core.utils.audit.SensitiveWordUtil;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.member.domain.MemberInfo;
import com.mwu.aitok.model.member.domain.UserSensitive;
import com.mwu.aitok.model.member.dto.LoginUserDTO;
import com.mwu.aitok.model.member.dto.RegisterBody;
import com.mwu.aitok.model.member.dto.UpdatePasswordDTO;
import com.mwu.aitok.model.member.vo.MemberInfoVO;
import com.mwu.aitok.servicememberoauth2.constants.UserCacheConstants;
import com.mwu.aitok.servicememberoauth2.entity.TokenPair;
import com.mwu.aitok.servicememberoauth2.repository.MemberInfoRepository;
import com.mwu.aitok.servicememberoauth2.repository.MemberRepository;
import com.mwu.aitok.servicememberoauth2.repository.UserSensitiveRepository;
import com.mwu.aitok.servicememberoauth2.security.JwtService;
import com.mwu.aitok.servicememberoauth2.service.MemberService;
import com.mwu.aitokcommon.cache.service.RedisService;
import com.mwu.aitokstarter.file.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.mwu.aitok.model.behave.mq.BehaveQueueConstant.BEHAVE_EXCHANGE;
import static com.mwu.aitok.model.behave.mq.BehaveQueueConstant.CREATE_ROUTING_KEY;
import static com.mwu.aitok.model.common.enums.HttpCodeEnum.*;


@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final UserSensitiveRepository sensitiveRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MemberInfoRepository memberInfoRepository;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final MinioService minioService;

    @Override
    public boolean register(RegisterBody registerBody) {
        if (StringUtils.isEmpty(registerBody.getUsername())) {
            throw new CustomException(USERNAME_NOT_NULL);
        }

        if (StringUtils.isEmpty(registerBody.getPassword())) {
            throw new CustomException(PASSWORD_NOT_NULL);
        }

        String username = registerBody.getUsername();

        Optional<Member> memberOptional = memberRepository.findByUserName(username);
        if(memberOptional.isPresent()){
            throw new CustomException(USERNAME_EXIST);
        }

        if (!registerBody.getPassword().equals(registerBody.getConfirmPassword())) {
            throw new CustomException(CONFIRM_PASSWORD_NOT_MATCH);
        }

        if (sensitiveCheck(registerBody.toString())) {
            throw new CustomException(HttpCodeEnum.SENSITIVEWORD_ERROR);
        }

      //  String uuid = UUID.randomUUID().toString();
        String fastUUID = IdUtils.fastUUID();
        String encodePassword = DigestUtils.md5DigestAsHex((registerBody.getPassword().trim() + fastUUID).getBytes());
        Member member = Member.builder()
                .userName(username)
                .nickName(IdUtils.shortUUID())
                .password(encodePassword)
                .salt(fastUUID)

                .build();

        try {
            Member saved =  memberRepository.save(member);
            String msg = saved.getUserId().toString();
            System.out.println("Registered user ID: " + msg);
            rabbitTemplate.convertAndSend(BEHAVE_EXCHANGE, CREATE_ROUTING_KEY, msg);

            MemberInfo memberInfo = new MemberInfo();
            memberInfo.setUserId(saved.getUserId());
            memberInfoRepository.save(memberInfo);
            return  true;

        } catch (Exception e) {
            throw new CustomException(null);
        }

    }

    // TODO: can work with redis to check if it already logged in
    // TODO: if a user tries to login in 3 times with wrong password, lock the account for 15 minutes also can work with redis
    @Override
    public Map<String, String> login(LoginUserDTO loginUserDTO) {






        if (StringUtils.isBlank(loginUserDTO.getUsername()) || StringUtils.isBlank(loginUserDTO.getPassword())) {
            throw new CustomException(SYSTEM_ERROR);
        }

        String username = loginUserDTO.getUsername();
        System.out.println("username: " + username);
        Optional<Member> memberOptional = memberRepository.findByUserName(username);
        if (memberOptional.isEmpty()) {
            System.out.println("Member not found" +username);
            throw new CustomException(USER_NOT_EXISTS);
        }

        Member member = memberOptional.get();

        Member userCache = redisService.getCacheObject(UserCacheConstants.USER_INFO_PREFIX + member.getUserId() );
        if (userCache != null) {
            throw new RuntimeException("用户缓存异常，请稍后重试");
        }

        System.out.println("userCache:eeeeeeeeeeeeeeeeeeeeeee " + userCache);
        Map<String, String> tokenMap = new HashMap<>();
        String salt = member.getSalt();
        String password = DigestUtils.md5DigestAsHex((loginUserDTO.getPassword().trim() + salt).getBytes());
        if (!password.equals(member.getPassword())) {
            throw new CustomException(PASSWORD_ERROR);
        }
        try {
            recordLoginUserInfo(member.getUserId());
            TokenPair token = jwtService.createAndStoreTokens(username, String.valueOf(member.getUserId()));
            System.out.println("token: " + token.accessToken());
            System.out.println("token: " + token.refreshToken());
            try {
                redisService.setCacheObject(UserCacheConstants.USER_INFO_PREFIX + member.getUserId(), member);
                redisService.expire(UserCacheConstants.USER_INFO_PREFIX + member.getUserId(), UserCacheConstants.USER_INFO_EXPIRE_TIME, TimeUnit.SECONDS);
            } catch (Exception ex) {
                // 打印完整异常以便排查（生产可改为 logger）
                System.err.println("Failed to write user cache to Redis: " + ex.getMessage());
                ex.printStackTrace();
                // 不抛出，允许登录继续
            }
//            System.out.println("Storing user info in Redis cache00000000000000000");
//            redisService.setCacheObject(UserCacheConstants.USER_INFO_PREFIX + member.getUserId(), member);
//            redisService.expire(UserCacheConstants.USER_INFO_PREFIX + member.getUserId(), UserCacheConstants.USER_INFO_EXPIRE_TIME, TimeUnit.SECONDS);
//
//            System.out.println("Storing user info in Redis cache111111111111111111111111111111111111111111111111111111111111   ");
            tokenMap.put("access_token", token.accessToken());
            tokenMap.put("refresh_token", token.refreshToken());
            System.out.println("tokenMap: " + tokenMap);
            return tokenMap;
        }  catch (Exception e) {
            throw new CustomException(SYSTEM_ERROR);
        }


    }

    @Override
    public Member updateUserInfo(Member user) {

        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication: " + authentication1.getPrincipal());


        Jwt jwt1 = ((JwtAuthenticationToken) authentication1).getToken();
        String userId = jwt1.getClaim("userid");
        Optional<Member> memberOptional = memberRepository.findByUserId(Long.valueOf(userId));
        if(memberOptional.isEmpty()) {
            throw new CustomException(USER_NOT_EXISTS);
        }

        if (sensitiveCheck(user.toString())) {
            throw new CustomException(HttpCodeEnum.SENSITIVEWORD_ERROR);
        }

        redisService.deleteObject(UserCacheConstants.USER_INFO_PREFIX + userId);

        try {
        Member member = memberOptional.get();
        member.setNickName(user.getNickName());
        member.setAvatar(user.getAvatar());
        member.setSex(user.getSex());
        member.setTelephone(user.getTelephone());
        member.setEmail(user.getEmail());
        member.setUpdateTime(LocalDateTime.now());
        memberRepository.save(member);
            redisService.setCacheObject(UserCacheConstants.USER_INFO_PREFIX + member.getUserId(), member);
            redisService.expire(UserCacheConstants.USER_INFO_PREFIX + member.getUserId(), UserCacheConstants.USER_INFO_EXPIRE_TIME, TimeUnit.SECONDS);

        return member;
        } catch (Exception e) {
            throw new CustomException(SYSTEM_ERROR);
        }
    }

    @Override
    public MemberInfoVO getUserFromCache(Long userId) {


            Member userCache = redisService.getCacheObject(UserCacheConstants.USER_INFO_PREFIX + userId);

            if (userCache == null) {
                Member user = memberRepository.findById(userId).orElse(null);
                if (user == null) {
                    throw new CustomException(USER_NOT_EXISTS);
                }
                user.setSalt(null);
                user.setPassword(null);
                redisService.setCacheObject(UserCacheConstants.USER_INFO_PREFIX + userId, user);
                redisService.expire(UserCacheConstants.USER_INFO_PREFIX + userId, UserCacheConstants.USER_INFO_EXPIRE_TIME, TimeUnit.SECONDS);
                MemberInfo memberInfoNew = new MemberInfo();
                BeanUtils.copyProperties(user, memberInfoNew);
                MemberInfoVO memberInfoVO = new MemberInfoVO();
                BeanUtils.copyProperties(memberInfoNew, memberInfoVO);
                return memberInfoVO;

            }

            Optional<MemberInfo> memberInfo = memberInfoRepository.findMemberInfoByUserId(userId);
            MemberInfoVO memberInfoVO = new MemberInfoVO();
            if (memberInfo.isPresent()) {
                BeanUtils.copyProperties(memberInfo.get(), memberInfoVO);
            } else {
                BeanUtils.copyProperties(userCache, memberInfoVO);
            }
            return memberInfoVO;


    }

    @Override
    public Boolean updatePass(UpdatePasswordDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication: " + authentication.getPrincipal());
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        String userId = jwt.getClaim("userid");
        if (StringUtils.isNull(userId)) {
            throw new CustomException(NEED_LOGIN);
        }
        if (StringUtils.isBlank(dto.getOldPassword()) || StringUtils.isBlank(dto.getNewPassword()) || StringUtils.isBlank(dto.getConfirmPassword())) {
            throw new CustomException(SYSTEM_ERROR);
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new CustomException(CONFIRM_PASSWORD_NOT_MATCH);
        }
        Optional<Member> memberOptional = memberRepository.findByUserId(Long.valueOf(userId));
        if(memberOptional.isEmpty()) {
            throw new CustomException(USER_NOT_EXISTS);
        }
        
        String salt = memberOptional.get().getSalt();
        String password = DigestUtils.md5DigestAsHex((dto.getOldPassword().trim() + salt).getBytes());
        if (!password.equals(memberOptional.get().getPassword())) {
            throw new CustomException(PASSWORD_ERROR);
        }
        
        Member membernew = memberOptional.get();
        membernew.setPassword(DigestUtils.md5DigestAsHex((dto.getNewPassword().trim() + salt).getBytes()));
        memberRepository.save(membernew);
        redisService.deleteObject(UserCacheConstants.USER_INFO_PREFIX + userId);

        redisService.setCacheObject(UserCacheConstants.USER_INFO_PREFIX + membernew.getUserId(), membernew);
        redisService.expire(UserCacheConstants.USER_INFO_PREFIX + membernew.getUserId(), UserCacheConstants.USER_INFO_EXPIRE_TIME, TimeUnit.SECONDS);

        return null;
    }

    @Override
    public String saveAvatar(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isNull(originalFilename)) {
            throw new CustomException(HttpCodeEnum.IMAGE_TYPE_FOLLOW);
        }
        try {
        //对原始文件名进行判断
        if (originalFilename.endsWith(".png")
                || originalFilename.endsWith(".jpg")
                || originalFilename.endsWith(".jpeg")
                || originalFilename.endsWith(".webp")) {
            return minioService.uploadFile(file);
        } else {
            throw new CustomException(HttpCodeEnum.IMAGE_TYPE_FOLLOW);
        }
        } catch (Exception e) {
            throw new RuntimeException("minio upload error");
        }
 
    }

    @Override
    public Member getByMemberId(Long memberId) {
        Optional<Member> member = memberRepository.findById(memberId);
        if (member.isEmpty()) return null;
        return member.get();

        //return memberRepository.findById(memberId).orElse(null);
    }

    @Override
    public Member getByUserName(String userName) {
        return memberRepository.findByUserName(userName).orElse(null);
    }

    public void recordLoginUserInfo(Long userId) {
        Optional<Member> optionalMember = memberRepository.findById(userId);
        if (optionalMember.isEmpty()) {
            throw new CustomException(USER_NOT_EXISTS);
        }
        Member user  = optionalMember.get();
        user.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        user.setLoginDate(LocalDateTime.now());
        memberRepository.save(user);
    }

    private boolean sensitiveCheck(String str) {

        List<UserSensitive> sensitiveList = sensitiveRepository.findAll();
        List<String> sensitiveWords = sensitiveList.stream()
                .map(UserSensitive::getSensitives)
                .toList();
        SensitiveWordUtil.initMap(sensitiveWords);
        //是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(str);
        // 存在敏感词
        return map.size() > 0;
    }
}

