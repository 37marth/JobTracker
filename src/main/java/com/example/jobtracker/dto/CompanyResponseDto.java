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

    public static CompanyResponseDto  createCompanyDto(Company company){
        return new CompanyResponseDto(
                company.getId(),
                company.getName(),
                company.getLocation());
    }
}