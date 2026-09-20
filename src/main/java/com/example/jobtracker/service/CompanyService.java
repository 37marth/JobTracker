package com.example.jobtracker.service;

import com.example.jobtracker.dto.CompanyRequestDto;
import com.example.jobtracker.entity.Company;
import com.example.jobtracker.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepository;


    public Company create(CompanyRequestDto companyRequestDto) {
//        1.dto.toEntity()가 반환한 객체를 Company 타입 변수에 담기.
          Company target =companyRequestDto.toEntity();
//        2.그 변수를 companyRepository.save(...)에 전달하고, save()의 반환값을 return하기.
          return companyRepository.save(target);
    }
}
