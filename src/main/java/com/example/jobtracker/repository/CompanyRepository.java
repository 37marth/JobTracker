package com.example.jobtracker.repository;

import com.example.jobtracker.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CompanyRepository extends JpaRepository<Company,Long> {
    // 회사 ID가 작은 순서대로 전체 목록을 조회.
    List<Company> findAllByOrderByIdAsc();
    // 같은 이름의 회사가 이미 등록되어 있는지 확인한다.
    boolean existsByName(String name);
    // 수정 중인 회사를 제외하고 같은 이름의 회사가 있는지 확인한다.
    boolean existsByNameAndIdNot(String name, Long id);
}
