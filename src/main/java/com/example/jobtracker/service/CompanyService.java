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


    // 요청 DTO를 회사 엔티티로 바꿔 DB에 저장하고, 저장 결과를 응답 DTO로 반환함.
    public CompanyResponseDto create(CompanyRequestDto companyRequestDto) {

          Company target =companyRequestDto.toEntity();
          Company savedCompany = companyRepository.save(target);
          return CompanyResponseDto.createCompanyDto(savedCompany);
    }

    // DB의 모든 회사를 조회하고, 각 엔티티를 응답 DTO로 바꿔 목록으로 반환함.
    public List<CompanyResponseDto> index() {
        List<Company> companies = companyRepository.findAll();
        return companies.stream()
                .map(company -> createCompanyDto(company))
                .collect(Collectors.toList());
    }

    // ID로 회사를 찾아 수정·저장한 뒤 응답 DTO를 반환함. 대상이 없으면 null을 반환함.
    public CompanyResponseDto update(Long id, CompanyRequestDto companyRequestDto) {
        Company target = companyRepository.findById(id).orElse(null);
        if (target != null) {
            target.patch(companyRequestDto);
            Company savedCompany = companyRepository.save(target);
            return CompanyResponseDto.createCompanyDto(savedCompany);
        }
        return null;
    }

    // 회사와 연결된 지원내역을 함께 삭제함. 컨트롤러의 결과 판단용으로 삭제한 회사 또는 null을 반환함.
    public Company delete(Long id) {

        Company target = companyRepository.findById(id).orElse(null);
        if (target != null) {
            companyRepository.delete(target);
        }
        return target;
    }
}
