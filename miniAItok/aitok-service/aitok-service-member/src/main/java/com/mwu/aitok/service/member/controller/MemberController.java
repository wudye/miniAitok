package com.mwu.aitok.service.member.controller;

import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.model.member.dto.RegisterBody;
import com.mwu.aitok.service.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member/test")
public class MemberController {

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


}
