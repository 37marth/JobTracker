package com.example.jobtracker.controller;

import com.example.jobtracker.dto.CompanyRequestDto;
import com.example.jobtracker.dto.CompanyResponseDto;
import com.example.jobtracker.entity.Company;

import com.example.jobtracker.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
public class CompanyApiController {
    @Autowired
    private CompanyService companyService;

    // 서비스에서 회사 목록을 받아 200 응답으로 보냄.
    @GetMapping("/companies")
    public ResponseEntity<List<CompanyResponseDto>> indexCompanies() {

        return ResponseEntity.status(HttpStatus.OK).body(companyService.index());
    }


    // 회사 등록을 서비스에 맡기고, 등록된 회사 DTO를 201 응답으로 보냄.
    @PostMapping("/companies")
    // @Valid: 요청 DTO의 @NotBlank 등 검증 조건을 검사함. 실패하면 메서드 실행 전에 400 응답.
    public ResponseEntity<CompanyResponseDto> create(@Valid @RequestBody CompanyRequestDto companyRequestDto) {
        CompanyResponseDto createdCompany = companyService.create(companyRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCompany);
    }

    // 회사 수정을 서비스에 맡기고, 성공하면 200과 회사 DTO, 대상이 없으면 404를 보냄.
    @PatchMapping("/companies/{id}")
    // @Valid: 수정 요청도 DTO의 검증 조건을 검사함. 대상 ID의 존재 여부 검사는 별도.
    public ResponseEntity<CompanyResponseDto> update(@PathVariable Long id, @Valid @RequestBody CompanyRequestDto companyRequestDto) {

        CompanyResponseDto companyResponseDto = companyService.update(id,companyRequestDto);
        // 없으면 404응답
        if (companyResponseDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(companyResponseDto);
    }

    // 회사 삭제를 서비스에 맡기고, 성공하면 본문 없는 204, 대상이 없으면 404를 보냄.
    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        Company company = companyService.delete(id);

        if (company == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



}
