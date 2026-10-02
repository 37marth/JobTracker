package com.example.jobtracker.dto;

import com.example.jobtracker.entity.Company;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CompanyRequestDto {
    // 검증 시 null, 빈 문자열(""), 공백만 있는 문자열을 거절함.
    @NotBlank(message = "회사명은 필수입니다.")
    private String name;
    private String location;

    // 입력받은 이름과 위치로 새 회사 엔티티를 만듦. DB 저장은 서비스에서 별도로 진행함.
    public Company toEntity() {
        return new Company(null,this.name,this.location);
    }
}
