package com.example.jobtracker.service;

import com.example.jobtracker.entity.Member;
import com.example.jobtracker.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberDetailsService implements UserDetailsService {
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Optional<Member> foundMember = memberRepository.findByLoginId(loginId);
        if (foundMember.isEmpty()) {
            throw new UsernameNotFoundException("회원을 찾을 수 없습니다.");
        }
        Member member = foundMember.get();
        return User.withUsername(member.getLoginId()) // ① 아이디 지정, 만들 준비 시작
                .password(member.getPassword())   // ② 비밀번호 해시 지정
                .roles("USER")                    // ③ 역할 지정
                .build();                        // ④ 준비한 값으로 객체 완성


    }
}