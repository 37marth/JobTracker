package com.example.jobtracker.entity;

import com.example.jobtracker.dto.JobApplicationRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @Column
    private String jobTitle;//지원한 직무

    @Column
    private LocalDateTime appliedAt;//지원 날짜+시간

    @Column
    private String memo;//남길 메모

    public static JobApplication createJobApplication(Company company, JobApplicationRequestDto jobApplicationRequestDto) {

        return new JobApplication(null,company,
                jobApplicationRequestDto.getJobTitle(),
                jobApplicationRequestDto.getAppliedAt(),
                jobApplicationRequestDto.getMemo());
    }
}
