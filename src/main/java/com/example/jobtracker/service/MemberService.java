package com.example.jobtracker.service;

import com.example.jobtracker.dto.SignupRequestDto;
import com.example.jobtracker.entity.Member;
import com.example.jobtracker.exception.DuplicateLoginIdException;
import com.example.jobtracker.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void signUp(SignupRequestDto signupRequestDto) {
        String loginId = signupRequestDto.getLoginId();
        boolean isExist = memberRepository.existsByLoginId(loginId);
        if (isExist) {
            throw new DuplicateLoginIdException("이미 존재하는 아이디입니다.");
        }
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());//패스워드를 해쉬값 변환함.
        Member member = signupRequestDto.toEntity(encodedPassword);
        memberRepository.save(member);
    }


}
