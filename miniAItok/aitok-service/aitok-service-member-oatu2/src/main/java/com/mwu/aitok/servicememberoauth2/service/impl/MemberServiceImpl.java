package com.mwu.aitok.servicememberoauth2.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mwu.aitiokcoomon.core.context.UserContext;
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
import com.mwu.aitok.servicememberoauth2.config.RedisLoginRateLimiter;
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
import org.apache.commons.lang.BooleanUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
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
    private final ObjectMapper objectMapper;


    //private final RedisLoginRateLimiter rateLimiter;


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

            rabbitTemplate.convertAndSend(BEHAVE_EXCHANGE, CREATE_ROUTING_KEY, msg);
            System.out.println("Sent message to RabbitMQ: " + msg);

            MemberInfo memberInfo = new MemberInfo();
            memberInfo.setUserId(saved.getUserId());
            memberInfoRepository.save(memberInfo);
            return  true;

        } catch (Exception e) {
            throw new CustomException(null);
        }

    }
    /*

    access token：短期有效（例如 5-15 分钟），包含用户 id、deviceId、jti 等声明，用于鉴权请求。
refresh token：长期有效（例如 7-30 天），用于换取新的 access token，可选择“刷新旋转”（每次换 token 都发新 refresh 并废弃旧的）。
存储：在 Redis 中按用户+设备保存 refresh token（或其 jti），支持多端并存：key 形如 USER_TOKEN:{userId}:{deviceId}。也可再保存 REFRESH_JTI:{jti} -> {userId}:{deviceId} 便于校验与回收。
多端并存：客户端在登录/刷新时必须传 deviceId（或者通过 User-Agent + 自生成 id 组合），后端根据 userId:deviceId 单独维护会话。
失败降级（fallback）：当 Redis 不可用时可以仍签发 access token 但不写缓存；这样 refresh 会失败并触发重新登录（即降级为无持久刷新），或告知客户端重新登录。
刷新流程：校验 refresh token 签名 -> 检查 Redis 中对应 jti 是否存在且匹配 userId:deviceId -> 生成新 access（可旋转 refresh） -> 更新 Redis。
注销：删除 USER_TOKEN:{userId}:{deviceId} 和对应 REFRESH_JTI:{jti}。
     */

    // TODO: 为什么普通页与隐身页能同时登录：隐身窗口有独立的 Cookie/localStorage，登录会生成新的 session 或 token，后端若允许多会话就接受该新会话而不踢掉旧会话。
    //实现方式：后端为每次登录创建一条会话记录（存 Redis 或数据库），记录 user_id、session_id/jwt、device_id、ip、user_agent、创建时间、过期时间。请求校验时只验证该 session 是否在表中且未过期/未被撤销。
    //允许多会话的后果：用户可在多设备/多窗口并行登录；需要提供会话管理（列出并撤销会话）、登出逻辑、并注意并发与安全策略。
    //若要限制为单会话：在登录时查找该用户已有会话并将其标记为失效或删除，新登录后只保留最新会话（可按 user_id 或 user_id+device_id 决定粒度
    @Override
    public Map<String, String> login(LoginUserDTO loginUserDTO, String ip) {

        // 检查用户名和IP是否被锁定
//        if (!rateLimiter.isAllowed("LOGIN:FAIL:USER:", loginUserDTO.getUsername(), 5, Duration.ofMinutes(15), Duration.ofMinutes(15))) {
//            throw new CustomException(HttpCodeEnum.USER_LOCKED);
//        }
        
//        if (!rateLimiter.isAllowed("LOGIN:FAIL:IP:", ip, 10, Duration.ofMinutes(15), Duration.ofMinutes(30))) {
//            throw new CustomException(HttpCodeEnum.IP_LOCKED);
//        }

        System.out.println("Login attempt from IP: " + ip + " for user: " + loginUserDTO.getUsername());

        if (StringUtils.isBlank(loginUserDTO.getUsername()) || StringUtils.isBlank(loginUserDTO.getPassword())) {
            throw new CustomException(SYSTEM_ERROR);
        }

        // TODO: here can set a more complex way to set and get cache to do multiple login
        // like the key is with ip + deviceId  +  username + accessToken + user_agent + createTime + expireTime
        // if user login again with different device or ip or user_agent , then create a new cache object
        // else update the old cache object with new accessToken and expireTime
        // userCache = redisService.getCacheObject(UserCacheConstants.USER_INFO_PREFIX + member.getUserId());



        String username = loginUserDTO.getUsername();
        Optional<Member> memberOptional = memberRepository.findByUserName(username);
        System.out.println("User lookup for username: " + username + ", found: " + memberOptional.isPresent());
        if (memberOptional.isEmpty()) {
            // 记录失败尝试（用户不存在）
            //recordFailedAttempt(loginUserDTO.getUsername(), null, ip);
            throw new CustomException(USER_NOT_EXISTS);
        }

        Member member = memberOptional.get();


        try {

            //Redis 的 DEL/delete 对不存在的 key 是幂等的：会返回 0/false，但不会报错。Java 层取决于你用的客户端实现——大多数 delete/del 方法对不存
            // 在的 key 也不会抛异常，只有在网络/连接错误时会抛异常。因此不需要先做 exists（反而多一次网络开销）。
           // userCache = redisService.getCacheObject(UserCacheConstants.USER_INFO_PREFIX + member.getUserId());
            redisService.deleteObject(UserCacheConstants.USER_INFO_PREFIX + member.getUserId());
        } catch (Exception ex) {
            // Redis 连接失败或操作异常，记录并继续（降级为无缓存模式）
            System.err.println("Unable to connect to Redis: " + ex.getMessage());
            ex.printStackTrace();

        }

        Map<String, String> tokenMap = new HashMap<>();
        String salt = member.getSalt();

        String password = DigestUtils.md5DigestAsHex((loginUserDTO.getPassword().trim() + salt).getBytes());
        System.out.println("Computed password hash: " + password + " for user: "  + member.getPassword());
        if (!password.equals(member.getPassword())) {
            // 记录失败尝试
            recordFailedAttempt(loginUserDTO.getUsername(), member.getUserId(), ip);
            throw new CustomException(PASSWORD_ERROR);
        }
        try {
            recordLoginUserInfo(member.getUserId());
            TokenPair token = null;
            try {
               token  = jwtService.createAndStoreTokens(username, String.valueOf(member.getUserId()));
            } catch (Exception ex) {
                // 打印完整异常以便排查（生产可改为 logger）
                System.err.println("p   : " + ex.getMessage());
                ex.printStackTrace();
                // 不抛出，允许登录继续
            }

            try {
                System.out.println("Writing user cache to Redis for userId: " + member.getUserId());
                // TODO: here can set a more complex way to set and get cache to do multiple login
                redisService.setCacheObject(UserCacheConstants.USER_INFO_PREFIX + member.getUserId(), member);
                redisService.expire(UserCacheConstants.USER_INFO_PREFIX + member.getUserId(), UserCacheConstants.USER_INFO_EXPIRE_TIME, TimeUnit.SECONDS);
            } catch (Exception ex) {
                // 打印完整异常以便排查（生产可改为 logger）
                System.err.println("Failed to write user cache to Redis: " + ex.getMessage());
                ex.printStackTrace();
                // 不抛出，允许登录继续
            }




            tokenMap.put("access_token", token.accessToken());
            tokenMap.put("refresh_token", token.refreshToken());
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
//            redisService.setCacheObject(UserCacheConstants.USER_INFO_PREFIX + member.getUserId(), member);
//            redisService.expire(UserCacheConstants.USER_INFO_PREFIX + member.getUserId(), UserCacheConstants.USER_INFO_EXPIRE_TIME, TimeUnit.SECONDS);

        return member;
        } catch (Exception e) {
            throw new CustomException(SYSTEM_ERROR);
        }
    }

    @Override
    public MemberInfoVO getUserFromCache(Long userId) {


        String te = UserCacheConstants.USER_INFO_PREFIX + userId;
        System.out.println("Fetching user from cache for userId:aaaaaaaaaaaaaaaaaa " + te);

        Object cachedObj = redisService.getCacheObject(UserCacheConstants.USER_INFO_PREFIX + userId);
        Member userCache = null;

        if (cachedObj != null) {
            System.out.println("Cached object found for userId: " + cachedObj);
            if (cachedObj instanceof Member) {
                userCache = (Member) cachedObj;
            } else if (cachedObj instanceof Map) {
                // 使用 ObjectMapper 转换 Map -> Member，并注册 JavaTimeModule 以支持 LocalDateTime
                /*
                ObjectMapper mapper = objectMapper.copy();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                userCache = mapper.convertValue(cachedObj, Member.class);

                 */
                userCache = objectMapper.convertValue(cachedObj, Member.class);
            } else {
                System.out.println("Cached object is unexpected type: " + cachedObj.getClass());
                throw new RuntimeException("Cached object type mismatch");
            }
        }
        if (userCache != null) {
            System.out.println("User cache hit for userIdddddddddddddddddddd: " + te);
            Optional<MemberInfo> memberInfo = memberInfoRepository.findMemberInfoByUserId(userId);
            MemberInfoVO memberInfoVO = new MemberInfoVO();
            if (memberInfo.isPresent()) {
                BeanUtils.copyProperties(memberInfo.get(), memberInfoVO);
            } else {
                throw       new RuntimeException("member info not found");
            }
            return memberInfoVO;
        }



        Member user = memberRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new CustomException(USER_NOT_EXISTS);
        }
        Member saveUser = new Member();
        BeanUtils.copyProperties(user, saveUser);
        saveUser.setSalt(null);
        saveUser.setPassword(null);

        redisService.setCacheObject(UserCacheConstants.USER_INFO_PREFIX + userId, saveUser);
        redisService.expire(UserCacheConstants.USER_INFO_PREFIX + userId, UserCacheConstants.USER_INFO_EXPIRE_TIME, TimeUnit.SECONDS);
        Optional<MemberInfo> memberInfoObject  = memberInfoRepository.findMemberInfoByUserId(userId);
        MemberInfoVO memberInfoVO = new MemberInfoVO();
        memberInfoObject.ifPresent(info -> BeanUtils.copyProperties(info, memberInfoVO));

        return memberInfoVO;

    }

    @Override
    public Boolean updatePass(UpdatePasswordDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
        /*
        redisService.deleteObject(UserCacheConstants.USER_INFO_PREFIX + userId);

        redisService.setCacheObject(UserCacheConstants.USER_INFO_PREFIX + membernew.getUserId(), membernew);
        redisService.expire(UserCacheConstants.USER_INFO_PREFIX + membernew.getUserId(), UserCacheConstants.USER_INFO_EXPIRE_TIME, TimeUnit.SECONDS);


         */
        return null;
    }

    @Override
    public String saveAvatar(MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
        String userId = jwt.getClaim("userid");
        if (StringUtils.isNull(userId)) {
            throw new CustomException(NEED_LOGIN);
        }
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isNull(originalFilename)) {
            throw new CustomException(HttpCodeEnum.IMAGE_TYPE_FOLLOW);
        }
        System.out.println("Original filename: " + originalFilename + " for userId: " + userId);
        try {
        //对原始文件名进行判断
        if (originalFilename.endsWith(".png")
                || originalFilename.endsWith(".jpg")
                || originalFilename.endsWith(".jpeg")
                || originalFilename.endsWith(".webp")) {
            String url = minioService.uploadFile(file);
            System.out.println("MinIO upload returned URL: " + url + " for userId: " + userId);
            Member member = getByMemberId(Long.valueOf(userId));
            if (member == null) {
                throw new CustomException(USER_NOT_EXISTS);
            }
            System.out.println("Uploaded avatar URL: " + url + " for userId: " + userId);
            member.setAvatar(url);
            return url;
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

    /**
     * 记录登录失败尝试
     * @param username 用户名
     * @param userId 用户ID（可为null，当用户不存在时）
     * @param ip 登录IP
     */
    private void recordFailedAttempt(String username, Long userId, String ip) {
        try {
            // 记录用户失败次数（5次失败锁定15分钟）
//            rateLimiter.isAllowed("LOGIN:FAIL:USER:", username, 5, Duration.ofMinutes(15), Duration.ofMinutes(15));
//
//            // 记录IP失败次数（10次失败锁定30分钟）
//            rateLimiter.isAllowed("LOGIN:FAIL:IP:", ip, 10, Duration.ofMinutes(15), Duration.ofMinutes(30));
            
            System.out.println("Login failed recorded for user: " + username + " from IP: " + ip);
        } catch (Exception ex) {
            System.err.println("Failed to record login attempt: " + ex.getMessage());
        }
    }
}

