package com.example.jobtracker.controller;

import com.example.jobtracker.dto.SignupRequestDto;
import com.example.jobtracker.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MemberApiController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/members/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequestDto signupRequestDto) {
        memberService.signUp(signupRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}