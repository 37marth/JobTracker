package com.example.jobtracker.controller;

import com.example.jobtracker.dto.CompanyResponseDto;
import com.example.jobtracker.dto.CompanyViewDto;
import com.example.jobtracker.dto.JobApplicationResponseDto;
import com.example.jobtracker.dto.PageNumberDto;
import com.example.jobtracker.service.CompanyService;
import com.example.jobtracker.service.JobApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String index(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        // 1.전달받은 페이지 번호로 회사 목록과 페이지 정보를 조회한다.
        Page<CompanyResponseDto> companyPage = companyService.indexPage(page);

        // 페이지 결과에서 회사 목록을 꺼낸다.
        List<CompanyResponseDto> companies = companyPage.getContent();
        //토탈페이지수
        int companiesTotalPages =  companyPage.getTotalPages();

        // 1. 페이지 링크 정보를 담을 빈 DTO 목록을 만든다.
        List<PageNumberDto> totalPages = new ArrayList<>();
        // 2. 번호를 1부터 시작해서 전체 페이지 수까지 반복한다.
        for(int i = 1; i <= companiesTotalPages; i++){
            // 3. 반복할 때마다 현재 번호를 목록에 추가한다.
            totalPages.add(new PageNumberDto(i,i-1));
        }


        // 2. 각 회사의 지원내역을 조회하고 회사 정보와 함께 묶는다.
        List<CompanyViewDto> companyViewsDto = new ArrayList<>();
        for (CompanyResponseDto company : companies) {
            List<JobApplicationResponseDto> applications =
                    jobApplicationService.index(company.getId());
            companyViewsDto.add(new CompanyViewDto(company, applications));
        }

        // 3. 템플릿이 회사와 그 회사의 지원내역을 함께 반복할 수 있게 전달한다.
        model.addAttribute("companies", companyViewsDto);

        // 코드의 페이지 번호는 0부터 시작하므로 화면에는 1을 더해서 표시한다.
        model.addAttribute("currentPage", page + 1);

        // 날짜 입력칸의 기본값으로 사용할 서버의 오늘 날짜를 전달한다.
        model.addAttribute("today", LocalDate.now());

        model.addAttribute("companiesTotalPages", companiesTotalPages);

        model.addAttribute("totalPages", totalPages);

        // 4. 회사별 지원내역이 포함된 HTML을 만들 템플릿 이름을 반환한다.
        return "home/home";
    }
}
