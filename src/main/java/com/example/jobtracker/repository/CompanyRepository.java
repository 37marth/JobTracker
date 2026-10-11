package com.example.jobtracker.repository;

import com.example.jobtracker.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CompanyRepository extends JpaRepository<Company,Long> {
    // 회사 ID가 작은 순서대로 전체 목록을 조회.
    List<Company> findAllByOrderByIdAsc();
}
