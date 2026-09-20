package com.example.jobtracker.dto;

import com.example.jobtracker.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CompanyRequestDto {
    private String name;
    private String location;

    public Company toEntity() {
        // 새 Company 객체를 만들어 반환하기
        return new Company(null,this.name,this.location);
    }
}
