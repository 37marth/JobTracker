package com.example.jobtracker.dto;

import lombok.Getter;

import java.util.List;

// 첫 화면에서 회사 정보와 그 회사의 지원내역을 함께 표시할 때 사용하는 DTO.
@Getter
public class CompanyViewDto {
    private final Long id;
    private final String name;
    private final String location;
    private final List<JobApplicationResponseDto> applications;

    // 회사 정보와 조회한 지원내역 목록을 하나의 화면용 객체에 담음.
    public CompanyViewDto(CompanyResponseDto company,
                          List<JobApplicationResponseDto> applications) {
        this.id = company.getId();
        this.name = company.getName();
        this.location = company.getLocation();
        this.applications = applications;
    }
}
