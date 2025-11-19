
package com.mwu.aitok.servicememberoauth2.controller;

import com.mwu.aitiokcoomon.core.constant.Constants;
import com.mwu.aitiokcoomon.core.context.UserContext;
import com.mwu.aitiokcoomon.core.domain.R;
import com.mwu.aitiokcoomon.core.exception.CustomException;
import com.mwu.aitiokcoomon.core.utils.EmailUtils;
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
import com.mwu.aitok.servicememberoauth2.constants.UserCacheConstants;
import com.mwu.aitok.servicememberoauth2.repository.MemberInfoRepository;
import com.mwu.aitok.servicememberoauth2.service.MemberService;
import com.mwu.aitokcommon.cache.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class MemberController {

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
    public R<Map<String, String>> login(@RequestBody LoginUserDTO loginUserDTO) throws Exception {
        log.debug("登录用户：{}", loginUserDTO);
        System.out.println("loginUserDTO: " + loginUserDTO);
        Map<String, String> token = memberService.login(loginUserDTO);

        return R.ok(token);
    }


    @PostMapping("/register")
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
        if (StringUtils.isNotEmpty(user.getTelephone()) && !PhoneUtils.isMobile(user.getTelephone())) {
            throw new CustomException(HttpCodeEnum.TELEPHONE_VALID_ERROR);
        }
        return R.ok(memberService.updateUserInfo(user));
    }

    @GetMapping("/{userId}")
    public R<MemberInfoVO> userInfoById(@PathVariable Long userId) {
        return R.ok(memberService.getUserFromCache(userId));
    }

    /**
     * 通过token获取用户信息
     */
    @GetMapping("/userinfo")
    public R<MemberInfoVO> userInfo(@AuthenticationPrincipal Jwt jwt) {
       // Long userId = UserContext.getUser().getUserId();
        Long userId = jwt.getClaim("userId");
        if (StringUtils.isNull(userId)) {
            R.fail(HttpCodeEnum.NEED_LOGIN.getCode(), "请先登录");
        }
        return R.ok(memberService.getUserFromCache(userId));
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
    @PostMapping("/avatar")
    public R<String> avatar(@RequestParam("file") MultipartFile file) {
        return R.ok(memberService.saveAvatar(file));
    }


}