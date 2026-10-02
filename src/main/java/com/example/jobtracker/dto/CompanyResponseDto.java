package com.example.jobtracker.dto;

import com.example.jobtracker.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompanyResponseDto {
    private Long id;
    private String name;
    private String location;

    // 회사 엔티티의 ID, 이름, 위치를 응답용 DTO에 옮겨 담음.
    public static CompanyResponseDto  createCompanyDto(Company company){
        return new CompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getLocation());
    }
}
