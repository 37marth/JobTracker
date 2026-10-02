package com.example.jobtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobApplicationRequestDto {
    // 검증 시 null, 빈 문자열(""), 공백만 있는 문자열을 거절함.
    @NotBlank(message = "지원 직무는 필수입니다.")
    private String jobTitle;
    private String memo;
    // 검증 시 null을 거절함. 날짜가 과거인지 미래인지까지 검사하지는 않음.
    @NotNull(message = "지원 날짜는 필수입니다.")
    private LocalDate appliedAt;

}
