package com.example.jobtracker.controller;

import com.example.jobtracker.dto.CompanyRequestDto;
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


    @PostMapping("/companies")
    public ResponseEntity<Company> createCompany(@Valid @RequestBody CompanyRequestDto companyRequestDto) {
        Company createdCompany = companyService.create(companyRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCompany);
    }

    @GetMapping("/companies")
    public ResponseEntity<List<Company>> indexCompanies() {
        return ResponseEntity.status(HttpStatus.OK).body(companyService.index());
    }
}
