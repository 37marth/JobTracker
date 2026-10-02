package com.example.jobtracker.service;

import com.example.jobtracker.dto.JobApplicationRequestDto;
import com.example.jobtracker.dto.JobApplicationResponseDto;
import com.example.jobtracker.dto.JobApplicationStatusRequestDto;
import com.example.jobtracker.entity.Company;
import com.example.jobtracker.entity.JobApplication;
import com.example.jobtracker.repository.CompanyRepository;
import com.example.jobtracker.repository.JobApplicationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.jobtracker.dto.JobApplicationResponseDto.createJobApplicationDto;

@Service
public class JobApplicationService {
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private CompanyRepository companyRepository;


    // 회사에 연결된 지원내역을 생성·저장하고 응답 DTO를 반환함. 회사가 없으면 404 예외를 발생시킴.
    public JobApplicationResponseDto create(Long companyId, JobApplicationRequestDto jobApplicationRequestDto) {
        // 1. companyId로 기존 회사를 조회한다.
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "등록되지 않은 회사입니다."
                ));
        //찾은 회사와 DTO의 값으로 지원내역 객체를 만든다.
        JobApplication jobApplication = JobApplication.createJobApplication(company,jobApplicationRequestDto);
        jobApplicationRepository.save(jobApplication);
        //resDTO형태로 변환하기
        JobApplicationResponseDto jobApplicationResponseDto = createJobApplicationDto(jobApplication);
        // 변환한 응답 DTO를 컨트롤러에 반환한다.
        return jobApplicationResponseDto;
    }

    // 해당 회사의 지원내역을 조회해 DTO 목록으로 반환함. 조회 결과가 없으면 빈 목록을 반환함.
    public List<JobApplicationResponseDto> index(Long companyId) {
        return jobApplicationRepository.findByCompanyId(companyId)
                .stream()
                .map(jobApplication -> createJobApplicationDto(jobApplication))
                .collect(Collectors.toList());
    }

    // ID로 지원내역을 찾아 수정·저장한 뒤 응답 DTO를 반환함. 대상이 없으면 null을 반환함.
    public JobApplicationResponseDto update(Long id, JobApplicationRequestDto jobApplicationRequestDto) {
        JobApplication target = jobApplicationRepository.findById(id).orElse(null);
        if (target != null) {
            target.patch(jobApplicationRequestDto);
            jobApplicationRepository.save(target);
            JobApplicationResponseDto jobApplicationResponseDto = createJobApplicationDto(target);
            return jobApplicationResponseDto;
        }
        return null;
    }

    // 지원내역 하나를 삭제함. 컨트롤러의 결과 판단용으로 삭제한 지원내역 또는 null을 반환함.
    public JobApplication delete(Long id) {
        JobApplication target = jobApplicationRepository.findById(id).orElse(null);
        if (target == null) {
            return null;
        }
        jobApplicationRepository.delete(target);
        return target;
    }

    // 지원내역 엔티티의 상태만 변경·저장하고 응답 DTO를 반환함. 대상이 없으면 null을 반환함.
    public JobApplicationResponseDto updateStatus(Long id, JobApplicationStatusRequestDto jobApplicationStatusRequestDto) {
        JobApplication target = jobApplicationRepository.findById(id).orElse(null);
        if (target == null) {
            return null;
        }
        target.changeStatus(jobApplicationStatusRequestDto.getStatus());
        jobApplicationRepository.save(target);
        JobApplicationResponseDto jobApplicationResponseDto = createJobApplicationDto(target);
        return jobApplicationResponseDto;

    }
}
