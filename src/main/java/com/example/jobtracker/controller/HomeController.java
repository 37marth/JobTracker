package com.example.jobtracker.controller;

import com.example.jobtracker.dto.CompanyResponseDto;
import com.example.jobtracker.dto.CompanyViewDto;
import com.example.jobtracker.dto.JobApplicationResponseDto;
import com.example.jobtracker.service.CompanyService;
import com.example.jobtracker.service.JobApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private CompanyService companyService;
    @Autowired
    private JobApplicationService jobApplicationService;

    // 회사별 지원내역까지 준비해 Mustache가 첫 화면의 HTML을 만들도록 전달함.
    @GetMapping("/")
    public String index(Model model) {
        // 1. 서비스에서 회사 목록을 가져온다.
        List<CompanyResponseDto> companies = companyService.index();

        // 2. 각 회사의 지원내역을 조회하고 회사 정보와 함께 묶는다.
        List<CompanyViewDto> companyViewsDto = new ArrayList<>();
        for (CompanyResponseDto company : companies) {
            List<JobApplicationResponseDto> applications =
                    jobApplicationService.index(company.getId());
            companyViewsDto.add(new CompanyViewDto(company, applications));
        }

        // 3. 템플릿이 회사와 그 회사의 지원내역을 함께 반복할 수 있게 전달한다.
        model.addAttribute("companies", companyViewsDto);

        // 날짜 입력칸의 기본값으로 사용할 서버의 오늘 날짜를 전달한다.
        model.addAttribute("today", LocalDate.now());

        // 4. 회사별 지원내역이 포함된 HTML을 만들 템플릿 이름을 반환한다.
        return "home/home";
    }
}
