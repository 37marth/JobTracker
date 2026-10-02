package com.example.jobtracker.controller;

import com.example.jobtracker.dto.CompanyResponseDto;
import com.example.jobtracker.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private CompanyService companyService;
    // 회사 목록을 조회해 첫 화면 템플릿에 전달함.
    @GetMapping("/")
    public String index(Model model) {
        // 1. 서비스에서 회사 목록을 가져온다.
        List<CompanyResponseDto> companies = companyService.index();

        // 2. 템플릿에서 "companies"라는 이름으로 사용할 수 있게 전달한다.
        model.addAttribute("companies", companies);

        // 3. 회사 목록을 표시할 템플릿 이름을 반환한다.
        return "home/home";
    }
}
