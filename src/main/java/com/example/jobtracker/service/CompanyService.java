package com.example.jobtracker.service;

import com.example.jobtracker.dto.CompanyRequestDto;
import com.example.jobtracker.dto.CompanyResponseDto;
import com.example.jobtracker.entity.Company;
import com.example.jobtracker.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.jobtracker.dto.CompanyResponseDto.createCompanyDto;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepository;


    public CompanyResponseDto create(CompanyRequestDto companyRequestDto) {

          Company target =companyRequestDto.toEntity();
          Company savedCompany = companyRepository.save(target);
          return CompanyResponseDto.createCompanyDto(savedCompany);
    }

    public List<CompanyResponseDto> index() {
        List<Company> companies = companyRepository.findAll();
        return companies.stream()
                .map(company -> createCompanyDto(company))
                .collect(Collectors.toList());
    }

    public CompanyResponseDto update(Long id, CompanyRequestDto companyRequestDto) {
        Company target = companyRepository.findById(id).orElse(null);
        if (target != null) {
            target.patch(companyRequestDto);
            Company savedCompany = companyRepository.save(target);
            return CompanyResponseDto.createCompanyDto(savedCompany);
        }
        return null;
    }

    public Company delete(Long id) {

        Company target = companyRepository.findById(id).orElse(null);
        if (target != null) {
            companyRepository.delete(target);
        }
        return target;
    }
}
