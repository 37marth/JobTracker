package com.example.jobtracker.repository;

import com.example.jobtracker.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CompanyRepository extends JpaRepository<Company,Long> {
}
