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
    @NotBlank(message = "지원 직무는 필수입니다.")
    private String jobTitle;
    private String memo;
    @NotNull(message = "지원 날짜는 필수입니다.")
    private LocalDate appliedAt;

}
