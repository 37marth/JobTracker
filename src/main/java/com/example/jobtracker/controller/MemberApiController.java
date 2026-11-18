package com.example.jobtracker.controller;

import com.example.jobtracker.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberApiController {

    @Autowired
    private MemberService memberService;
}