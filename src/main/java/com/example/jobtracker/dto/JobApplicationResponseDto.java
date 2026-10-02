package com.example.jobtracker.dto;

import com.example.jobtracker.entity.JobApplication;
import com.example.jobtracker.entity.JobApplicationStatus;
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
    private JobApplicationStatus status;

    // 지원내역 엔티티를 응답용 DTO로 바꿈. 연결된 회사는 객체 대신 ID만 담음.
    public static JobApplicationResponseDto createJobApplicationDto(
            JobApplication jobApplication) {

        return new JobApplicationResponseDto(
                jobApplication.getId(),
                jobApplication.getCompany().getId(),
                jobApplication.getJobTitle(),
                jobApplication.getAppliedAt(),
                jobApplication.getMemo(),
                jobApplication.getStatus()
        );
    }
}
