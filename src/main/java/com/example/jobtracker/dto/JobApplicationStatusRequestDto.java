package com.example.jobtracker.dto;

import com.example.jobtracker.entity.JobApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
//상태 변경 요청만 받는 작은 DTO,JobApplicationRequestDto는 직무와 날짜까지 필수로 요구하기때문.
public class JobApplicationStatusRequestDto {

    // 상태 변경 요청에서 null이나 status 누락을 거절함.
    @NotNull(message = "지원 상태는 필수입니다.")
    private JobApplicationStatus status;
}