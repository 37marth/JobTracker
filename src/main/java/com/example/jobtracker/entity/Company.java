package com.example.jobtracker.entity;

import com.example.jobtracker.dto.CompanyRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    @Column
    private String location;

    public void patch(CompanyRequestDto companyRequestDto) {
        this.name = companyRequestDto.getName();
        this.location = companyRequestDto.getLocation();
    }
}
