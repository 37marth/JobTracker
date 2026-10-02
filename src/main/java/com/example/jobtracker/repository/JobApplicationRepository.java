package com.example.jobtracker.repository;

import com.example.jobtracker.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication,Long> {
    // 회사 ID가 일치하는 지원내역을 조회함. Spring Data JPA가 메서드 이름으로 조회를 구현함.
    List<JobApplication> findByCompanyId(Long companyId);
}
