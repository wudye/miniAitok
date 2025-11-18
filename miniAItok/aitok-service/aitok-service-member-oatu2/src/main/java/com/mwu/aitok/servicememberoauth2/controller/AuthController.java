package com.mwu.aitok.servicememberoauth2.controller;


import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.member.dto.RegisterBody;
import com.mwu.aitok.servicememberoauth2.entity.TokenPair;
import com.mwu.aitok.servicememberoauth2.repository.MemberRepository;
import com.mwu.aitok.servicememberoauth2.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/member/test")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    private MemberRepository memberRepository;
    @GetMapping("/test")
    public String test() {
        return "test hhhh";
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterBody memberRegisterDto) {
        System.out.println("start register...");

        System.out.println("memberRegisterDto: " + memberRegisterDto);
        System.out.println("memberRegisterDto: " + memberRegisterDto.getUsername());
        System.out.println("memberRegisterDto: " + memberRegisterDto.getPassword());

        Member member = new Member();
        member.setUserName(memberRegisterDto.getUsername());
        member.setPassword(memberRegisterDto.getPassword());
        memberRepository.save(member);
        return "register";

    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) throws Exception {

        System.out.println("login attempt: " + username + "/" + password);
        // TODO: 用真实用户验证替换此处
//        if (!"secret".equals(password)) {
//            return ResponseEntity.status(401).build();
//        }
        int userId = ThreadLocalRandom.current().nextInt(10000); // 正确：生成 0..999_999_999 的随机数
        TokenPair token = jwtService.createAndStoreTokens(username, String.valueOf(userId));
        System.out.println("token: " + token.accessToken());
        System.out.println("token: " + token.refreshToken());
        return ResponseEntity.ok().body(java.util.Map.of(
                "accessToken", token.accessToken(),
                "refreshToken", token.refreshToken()
        ));
    }

    // 可选：暴露 JWKs，Gateway 可以配置 jwk-set-uri 指向此端点
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<?> jwks() {
        return ResponseEntity.ok(jwtService.getJwkSet().toJSONObject());
    }


}
