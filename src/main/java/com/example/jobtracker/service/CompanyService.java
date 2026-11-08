package com.example.jobtracker.service;

import com.example.jobtracker.dto.CompanyRequestDto;
import com.example.jobtracker.dto.CompanyResponseDto;
import com.example.jobtracker.entity.Company;
import com.example.jobtracker.exception.DuplicateCompanyException;
import com.example.jobtracker.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.jobtracker.dto.CompanyResponseDto.createCompanyResponseDto;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepository;


    // 요청 DTO를 회사 엔티티로 바꿔 DB에 저장하고, 저장 결과를 응답 DTO로 반환함.
    public CompanyResponseDto create(CompanyRequestDto companyRequestDto) {

        Company target = companyRequestDto.toEntity();
        // 같은 회사 이름이 있으면 저장하지 않고 중복 예외를 던진다.
        boolean isExist = companyRepository.existsByName(target.getName());
        if (isExist) {
            throw new DuplicateCompanyException("이미 등록된 회사입니다.");
        }

        // 중복이 아닐 때만 저장하고 정상 응답 DTO를 반환한다.
        Company savedCompany = companyRepository.save(target);
        return CompanyResponseDto.createCompanyResponseDto(savedCompany);
    }

    // DB의 모든 회사를 조회하고, 각 엔티티를 응답 DTO로 바꿔 목록으로 반환함.
    public List<CompanyResponseDto> index() {
        List<Company> companies = companyRepository.findAllByOrderByIdAsc();
        return companies.stream()
                .map(company -> createCompanyResponseDto(company))
                .collect(Collectors.toList());
    }

    // 회사 목록을 한 페이지에 5개씩 최신 등록 순서로 조회한다.
    public Page<CompanyResponseDto> indexPage(int page) { // page는 0부터 시작하는 조회할 페이지 번호.

        // 전달받은 페이지 번호와 한 페이지의 개수를 조회 조건으로 만든다.
        PageRequest pageRequest = PageRequest.of(page, 5, Sort.by("id").descending()); // 최대 5개씩, ID 내림차순으로 조회.

        // 조회 조건에 맞는 회사들을 페이지 형태로 가져온다.
        Page<Company> companies = companyRepository.findAll(pageRequest);

        // 회사 엔티티를 응답 DTO로 바꾸고, 페이지 정보를 유지해서 반환한다.
        return companies.map(company -> createCompanyResponseDto(company));

    }

    // ID로 회사를 찾아 수정·저장한 뒤 응답 DTO를 반환함. 대상이 없으면 null을 반환함.
    public CompanyResponseDto update(Long id, CompanyRequestDto companyRequestDto) {
        Company target = companyRepository.findById(id).orElse(null);
        if (target != null) {
            boolean isExist = companyRepository.existsByNameAndIdNot(companyRequestDto.getName(), id);
            if (isExist) {
                throw new DuplicateCompanyException("이미 등록된 회사입니다.");
            }
            target.patch(companyRequestDto);
            Company savedCompany = companyRepository.save(target);
            return CompanyResponseDto.createCompanyResponseDto(savedCompany);
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
