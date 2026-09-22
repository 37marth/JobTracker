package com.example.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobApplicationRequestDto {
    private String jobTitle;
    private String memo;
    private LocalDateTime appliedAt;

}
