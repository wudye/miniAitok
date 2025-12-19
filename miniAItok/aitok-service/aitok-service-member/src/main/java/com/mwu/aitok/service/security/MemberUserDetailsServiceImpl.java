package com.mwu.aitok.service.security;



import com.mwu.aitok.service.member.repository.MemberRepository;
import com.mwu.aitok.model.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        Optional<Member> accountGet  = memberRepository.findByUserName(username);
        if (accountGet.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        Member member = accountGet.get();


        MemeberUserDetailsImpl accountDetails = new MemeberUserDetailsImpl();
        accountDetails.setUsername(member.getUserName());
        accountDetails.setPassword(member.getPassword());
        accountDetails.setRole("ROLE_USER");


        return accountDetails;
    }
}
