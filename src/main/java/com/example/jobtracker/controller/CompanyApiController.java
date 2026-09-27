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

    @GetMapping("/companies")
    public ResponseEntity<List<CompanyResponseDto>> indexCompanies() {

        return ResponseEntity.status(HttpStatus.OK).body(companyService.index());
    }


    @PostMapping("/companies")
    public ResponseEntity<CompanyResponseDto> create(@Valid @RequestBody CompanyRequestDto companyRequestDto) {
        CompanyResponseDto createdCompany = companyService.create(companyRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCompany);
    }

    @PatchMapping("/companies/{id}")
    public ResponseEntity<CompanyResponseDto> update(@PathVariable Long id, @Valid @RequestBody CompanyRequestDto companyRequestDto) {

        CompanyResponseDto companyResponseDto = companyService.update(id,companyRequestDto);
        // 없으면 404응답
        if (companyResponseDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(companyResponseDto);
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        Company company = companyService.delete(id);

        if (company == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



}
