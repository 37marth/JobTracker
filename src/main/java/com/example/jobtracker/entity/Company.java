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

    // 한 회사에 여러 지원내역이 연결됨. mappedBy는 JobApplication의 company 필드명.
    // REMOVE: JPA로 회사를 삭제하면 연결된 지원내역도 함께 삭제함.
    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE)
    // Lombok이 만드는 toString()에서 이 목록을 제외함. DB 저장이나 JSON 응답 설정과는 별개.
    @ToString.Exclude
    //지원내역을 받아줄 리스트.
    private List<JobApplication> jobApplications = new ArrayList<>();

    // 이 회사 객체의 이름과 위치를 요청 DTO의 값으로 바꿈. DB 저장은 서비스에서 진행함.
    public void patch(CompanyRequestDto companyRequestDto) {
        this.name = companyRequestDto.getName();
        this.location = companyRequestDto.getLocation();
    }

    // ID, 이름, 위치를 받아 회사 객체를 만듦. 지원내역 목록은 위에서 빈 목록으로 초기화함.
    // 목록까지 받는 @AllArgsConstructor 대신 기존의 세 인자 생성 방식을 유지함.
    public Company(Long id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }
}
