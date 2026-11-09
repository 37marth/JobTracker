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
            @RequestParam(defaultValue = "0") int page,//현재보고있는 페이지
            Model model) {

        // 음수 페이지 번호가 들어오면 첫 페이지 주소로 다시 보낸다.
        if (page < 0) {
            return "redirect:/?page=0";
        }

        // 1.전달받은 페이지 번호로 회사 목록과 페이지 정보를 조회한다.
        Page<CompanyResponseDto> companyPage = companyService.indexPage(page);


        // 페이지 결과에서 회사 목록을 꺼낸다.
        List<CompanyResponseDto> companies = companyPage.getContent();

        //토탈 페이지수
        int companiesTotalPages =  companyPage.getTotalPages();

        if (companiesTotalPages > 0 && page >= companiesTotalPages ) {
            return "redirect:/?page="+(companiesTotalPages-1);
        }

        // 현재 묶음에서 화면에 표시할 첫 번호와 끝 번호.
        int startPage = (page / 5) * 5 + 1;
        int endPage = (page / 5) * 5 + 5;


        // 1. 페이지 링크 정보를 담을 빈 DTO 목록을 만든다.
        List<PageNumberDto> totalPages = new ArrayList<>();
        // 2. 현재 페이지가 속한 묶음의 첫 표시 번호부터 반복한다.
        // page / 5: 정수 나눗셈으로 나머지를 버려 묶음 번호를 구한다(0, 1, 2...).
        // * 5: 그 묶음의 시작 위치로 돌아간다. + 1: 화면 번호는 1부터 표시한다.
        // 예: 주소의 page=12(화면 13페이지) → 12 / 5 = 2 → 2 * 5 + 1 = 11.
        for(int i = startPage; i <= endPage && i<=companiesTotalPages; i++){

            // i는 내가 눈으로 보고 있는 화면의 페이지 번호이므로, 현재 요청 번호에 1을 더한 값과 비교한다.
            boolean current = i == page + 1;
            totalPages.add(new PageNumberDto(i, i - 1, current));
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

        // 이전 묶음의 첫 페이지로 이동할 요청 번호
        model.addAttribute("previousGroupPage", startPage-6);
        // 현재 묶음이 1페이지부터 시작하지 않으면 이전 묶음이 있다.
        model.addAttribute("hasPreviousGroup", startPage > 1);

        // 다음 묶음의 첫 페이지로 이동할 요청 번호.
        model.addAttribute("nextGroupPage", endPage);

        // 묶음 끝보다 실제 전체 페이지 수가 크면 다음 묶음이 있다.
        model.addAttribute("hasNextGroup", endPage < companiesTotalPages);

        // 첫 페이지를 보고 있지 않을 때 처음으로 가는 링크를 표시한다.
        model.addAttribute("hasFirstPage", page > 0);

        // 마지막 페이지를 보고 있지 않을 때 마지막으로 가는 링크를 표시한다.
        model.addAttribute("hasLastPage", page < companiesTotalPages - 1);

        //마지막페이지 전용
        model.addAttribute("goToLastPage", companiesTotalPages-1);

        //페이지가 존재하는가?(하나라도 회사가 있는가)
        model.addAttribute("hasPages", companiesTotalPages > 0);



        // 4. 회사별 지원내역이 포함된 HTML을 만들 템플릿 이름을 반환한다.
        return "home/home";
    }
}
