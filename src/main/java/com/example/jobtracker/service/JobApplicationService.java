package com.example.jobtracker.service;

import com.example.jobtracker.dto.JobApplicationRequestDto;
import com.example.jobtracker.entity.Company;
import com.example.jobtracker.entity.JobApplication;
import com.example.jobtracker.repository.CompanyRepository;
import com.example.jobtracker.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobApplicationService {
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private CompanyRepository companyRepository;


    public JobApplication create(Long companyId, JobApplicationRequestDto jobApplicationRequestDto) {
        // 1. companyId로 기존 회사를 조회한다.
        Company company = companyRepository.findById(companyId)
                // 2. 회사가 없으면 저장을 중단하고 오류로 처리한다.
                .orElseThrow(()->new IllegalArgumentException("조회 실패! 등록되지 않은 회사입니다."));
        // 3. 찾은 회사와 DTO의 값으로 지원내역 객체를 만든다.
        JobApplication jobApplication = JobApplication.createJobApplication(company,jobApplicationRequestDto);
        // 4. 지원내역을 저장하고 그 결과를 반환한다.
        return jobApplicationRepository.save(jobApplication);
    }
}
