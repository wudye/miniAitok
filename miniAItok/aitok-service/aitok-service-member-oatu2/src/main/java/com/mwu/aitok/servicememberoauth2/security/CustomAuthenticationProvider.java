package com.mwu.aitok.servicememberoauth2.security;


import com.mwu.aitok.model.member.domain.Member;
import com.mwu.aitok.servicememberoauth2.repository.MemberRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.DigestUtils;

import java.util.Collections;
import java.util.Optional;
public class CustomAuthenticationProvider {

}
/*
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final MemberRepository memberRepository;

    public CustomAuthenticationProvider(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = String.valueOf(authentication.getCredentials());

        Optional<Member> memberOpt = memberRepository.findByUserName(username);
        if (memberOpt.isEmpty()) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        Member member = memberOpt.get();

        String salt = member.getSalt();
        String hashed = DigestUtils.md5DigestAsHex((rawPassword.trim() + (salt == null ? "" : salt)).getBytes());
        if (!hashed.equals(member.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 认证通过：principal 放 Member（或自定义 UserDetails）
        return new UsernamePasswordAuthenticationToken(member, null, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
*/