package com.example.jobtracker.entity;

import com.example.jobtracker.dto.CompanyRequestDto;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@ToString
@Getter
@NoArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    @Column
    private String location;
    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)
    @ToString.Exclude
    private List<JobApplication> jobApplications = new ArrayList<>();

    public void patch(CompanyRequestDto companyRequestDto) {
        this.name = companyRequestDto.getName();
        this.location = companyRequestDto.getLocation();
    }

    //AllArgsConstructor는 모든 필드를 받는 생성자를 만드는데, 목록 필드를 추가하면 매개변수도 네 개로 늘어남. 그러면 기존의 new Company(null, 이름, 위치)에 빨간 줄이 생김.
    //Company의 @AllArgsConstructor와 해당 import를 지우고, 다음 생성자를 직접 추가해야함.
    public Company(Long id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }
}
