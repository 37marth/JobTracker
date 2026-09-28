package com.example.jobtracker.dto;

import com.example.jobtracker.entity.JobApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class JobApplicationResponseDto {

    private Long id;
    private Long companyId;
    private String jobTitle;
    private LocalDate appliedAt;
    private String memo;

    public static JobApplicationResponseDto createJobApplicationDto(
            JobApplication jobApplication) {

        return new JobApplicationResponseDto(
                jobApplication.getId(),
                jobApplication.getCompany().getId(),
                jobApplication.getJobTitle(),
                jobApplication.getAppliedAt(),
                jobApplication.getMemo()
        );
    }
}
