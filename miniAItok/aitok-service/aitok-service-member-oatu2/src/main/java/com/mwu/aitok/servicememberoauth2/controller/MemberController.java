
package com.mwu.aitok.servicememberoauth2.controller;

import com.mwu.aitiokcoomon.core.constant.Constants;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitiokcoomon.core.utils.EmailUtils;
import com.mwu.aitiokcoomon.core.utils.IpUtils;
import com.mwu.aitiokcoomon.core.utils.PhoneUtils;
import com.mwu.aitiokcoomon.core.utils.string.StringUtils;
import com.mwu.aitok.model.common.enums.HttpCodeEnum;
import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.member.domain.MemberInfo;
import com.mwu.aitok.model.member.dto.LoginUserDTO;
import com.mwu.aitok.model.member.dto.RegisterBody;
import com.mwu.aitok.model.member.dto.UpdatePasswordDTO;
import com.mwu.aitok.model.member.enums.LoginTypeEnum;
import com.mwu.aitok.model.member.vo.MemberInfoVO;
import com.mwu.aitok.servicememberoauth2.annotation.RateLimit;
import com.mwu.aitok.servicememberoauth2.config.RedisLoginRateLimiter;
import com.mwu.aitok.servicememberoauth2.constants.UserCacheConstants;
import com.mwu.aitok.servicememberoauth2.entity.TokenEntity;
import com.mwu.aitok.servicememberoauth2.entity.TokenPair;
import com.mwu.aitok.servicememberoauth2.multiDeviceLoginExample.DeviceDetector;
import com.mwu.aitok.servicememberoauth2.multiDeviceLoginExample.DeviceInfo;
import com.mwu.aitok.servicememberoauth2.repository.MemberInfoRepository;
import com.mwu.aitok.servicememberoauth2.repository.TokenRepository;
import com.mwu.aitok.servicememberoauth2.security.JwtService;
import com.mwu.aitok.servicememberoauth2.service.MemberService;
import com.mwu.aitokcommon.cache.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class MemberController {
    private final TokenRepository tokenRepository;
   // private final RedisLoginRateLimiter rateLimiter;
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(5);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);


    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<String> test(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request,
            Authentication authentication) {


        String username = jwt.getClaim("username");
        String userId = jwt.getClaim("userid");


        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication: " + authentication1.getPrincipal());

        Jwt jwt1 = ((JwtAuthenticationToken) authentication).getToken();
        System.out.println("jwt1: " + jwt1.getClaim("username"));
        System.out.println("jwt1: " + jwt1.getClaim("userid"));


        Jwt token = ((JwtAuthenticationToken) authentication).getToken();
        System.out.println("token00000: " + token.getClaim("username"));
        System.out.println("token0000: " + token.getClaim("userid"));


        // 如果 @AuthenticationPrincipal 没有注入 jwt，尝试从 Authentication 获取（防止 null）
        if (jwt == null && authentication instanceof JwtAuthenticationToken) {
            jwt = ((JwtAuthenticationToken) authentication).getToken();
        }

        Object idFromJwt = null;
        Object nameFromJwt = null;
        if (jwt != null) {
            idFromJwt = jwt.getClaim("userId");
            nameFromJwt = jwt.getClaim("username");
        } else {
            // 回退：使用网关注入的 headers（若网关已注入）
            idFromJwt = request.getHeader("X-User-Id");
            nameFromJwt = request.getHeader("X-Username");
        }

        if (idFromJwt == null && nameFromJwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("missing jwt and user headers");
        }

        idFromJwt = request.getHeader("X-User-Id");
        nameFromJwt = request.getHeader("X-Username");
        return ResponseEntity.ok("userId=" + idFromJwt + ", username=" + nameFromJwt);
    }


    @Autowired
    private MemberService memberService;



    /**
     * 登录
     *
     * @param loginUserDTO
     * @return
     */
    @PostMapping("/login")
    //@RateLimit(prefix = "LOGIN_API:", window = 60, count = 3, type = RateLimit.LimitType.IP)
    public R<Map<String, String>> login(@RequestBody LoginUserDTO loginUserDTO, HttpServletRequest request,HttpServletResponse response) throws Exception {


        String username = loginUserDTO.getUsername();
        String ip =  extractClientIp(request);
        String ip2 = IpUtils.getIpAddr(request);
        DeviceInfo deviceInfo = DeviceDetector.resolve(request);
        String deviceId = deviceInfo.getDeviceId();
        System.out.println(
                "Login attempt: username=" + username +
                        ", ip=" + ip +
                        ", ip2=" + ip2 +
                        ", deviceId=" + deviceId +
                        ", deviceType=" + deviceInfo.getDeviceType() +
                        ", userAgent=" + deviceInfo.getUserAgent() +
                        ", deviceIp=" + deviceInfo.getIp()

        );
        log.info("Login attempt: username={}, ip={}, deviceId={}", username, ip, deviceId);


//        boolean userAllowed = rateLimiter.isAllowed("LOGIN:FAIL:USER:", username, MAX_ATTEMPTS, WINDOW, LOCK_DURATION);
//        if (!userAllowed) {
//
//           return R.fail(HttpCodeEnum.TOO_MANY_REQUESTS.getCode(), "账号已被短期锁定，稍后再试");
//            //return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("账号已被短期锁定，稍后再试");
//        }
//        boolean ipAllowed = rateLimiter.isAllowed("LOGIN:FAIL:IP:", ip, MAX_ATTEMPTS * 4, WINDOW, LOCK_DURATION);
//        if (!ipAllowed) {
//            return R.fail(HttpCodeEnum.TOO_MANY_REQUESTS.getCode(), "所在 IP 被短期锁定，稍后再试");
//            //return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("所在 IP 被短期锁定，稍后再试");
//        }

        Map<String, String> token = memberService.login(loginUserDTO,  ip);

        /*
        this is for production use, we store refresh token in httpOnly cookie
        String refreshToken = token.get("refresh_token");

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true).secure(false).path("/member/api/v1/refresh").maxAge(Duration.ofDays(30)).sameSite("Lax").build();



        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        System.out.println("Set-Cookie: " + refreshCookie.toString());

         */

        return R.ok(token);
    }


    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }


    @PostMapping("/refresh")
    /*
    for production use, we store refresh token in httpOnly cookie
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                     HttpServletResponse response) throws Exception {

     */
    public ResponseEntity<?> refresh(@RequestBody String refreshToken,
            HttpServletResponse response) throws Exception {
        if (refreshToken == null ) {
            return ResponseEntity.status(401).build();
        }
        System.out.println("Received refresh token from cookie: " + refreshToken);

        TokenEntity    tokenEntity = jwtService.findByRefreshToken(refreshToken);
        if (tokenEntity == null || tokenEntity.getRefreshExpiry().isBefore(java.time.Instant.now())) {
            return ResponseEntity.status(401).build();
        }

        tokenRepository.deleteByRefreshToken(refreshToken);
        String username = tokenEntity.getUsername();
        String userId = tokenEntity.getUserId();


        TokenPair newAccess = jwtService.createAndStoreTokens(username, userId);
        String newAccessToken = newAccess.accessToken();
        String newRefreshToken = newAccess.refreshToken();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken",newRefreshToken)
                .httpOnly(true).secure(false).path("/api/auth/refresh").maxAge(Duration.ofDays(30)).sameSite("Lax").build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token; // ensure cookie is set and return token if client needs it in body
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody String refreshToken,  HttpServletResponse response) {
      /*
        ResponseCookie clearAccess = ResponseCookie.from("accessToken", "").httpOnly(true).secure(false).path("/").maxAge(0).build();
        ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "").httpOnly(true).secure(false).path("/api/auth/refresh").maxAge(0).build();
        ResponseCookie clearXsrf = ResponseCookie.from("XSRF-TOKEN", "").httpOnly(false).path("/").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearXsrf.toString());


       */

        tokenRepository.deleteByRefreshToken(refreshToken);
        return ResponseEntity.ok(Map.of("status", "logged out"));

    }



    @PostMapping("/register")
    //@RateLimit(prefix = "REGISTER_API:", window = 3600, count = 5, type = RateLimit.LimitType.IP)
    public R<Boolean> register(@RequestBody RegisterBody registerBody) {
        log.debug("register user：{}", registerBody);
        boolean b = memberService.register(registerBody);
        return R.ok(b);
    }


    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public R<Member> save(@RequestBody Member user) {
        // 校验邮箱
        if (StringUtils.isNotEmpty(user.getEmail()) && !EmailUtils.isValidEmail(user.getEmail())) {
            throw new CustomException(HttpCodeEnum.EMAIL_VALID_ERROR);
        }
        // 校验手机号
//        if (StringUtils.isNotEmpty(user.getTelephone()) && !PhoneUtils.isMobile(user.getTelephone())) {
//            throw new CustomException(HttpCodeEnum.TELEPHONE_VALID_ERROR);
//        }
        return R.ok(memberService.updateUserInfo(user));
    }

    @GetMapping("/{userId}")
    public R<MemberInfoVO> userInfoById(@PathVariable Long userId) {
        System.out.println("Fetching user info for userId: " + userId);
        return R.ok(memberService.getUserFromCache(userId));
    }

    /**
     * 通过token获取用户信息
     */
    @GetMapping("/userinfo")
    public R<MemberInfoVO> userInfo(@AuthenticationPrincipal Jwt jwt) {
       // Long userId = UserContext.getUser().getUserId();
        System.out.println("Fetching user info using token");
        String userId = jwt.getClaim("userid");
        System.out.println("Extracted userId from JWT: " + userId);
        if (StringUtils.isNull(userId)) {
            R.fail(HttpCodeEnum.NEED_LOGIN.getCode(), "请先登录");
        }
        return R.ok(memberService.getUserFromCache(Long.valueOf(userId)));
    }

    @PostMapping("/updatepass")
    public R<Boolean> updatePass(@RequestBody UpdatePasswordDTO dto) {
        return R.ok(memberService.updatePass(dto));
    }


    /**
     * 头像上传
     *
     * @param file 图片文件，大小限制1M
     * @return url
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> avatar(@RequestPart(value = "file") MultipartFile file) {
        System.out.println(file.getSize());
        System.out.println(file.getOriginalFilename());
        return R.ok(memberService.saveAvatar(file));
    }


}