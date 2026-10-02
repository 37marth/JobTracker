package com.example.jobtracker.entity;

import com.example.jobtracker.dto.JobApplicationRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 여러 지원내역이 같은 회사 하나를 참조할 수 있음.
    @ManyToOne
    // 지원내역 테이블의 company_id 외래키 컬럼에 연결된 회사의 ID를 저장함.
    @JoinColumn(name = "company_id")
    private Company company;

    @Column
    private String jobTitle;//지원한 직무

    @Column
    private LocalDate appliedAt;//지원 날짜

    @Column
    private String memo;//남길 메모

    // enum의 순서 번호 대신 APPLIED, INTERVIEW 같은 이름을 DB에 문자열로 저장함.
    @Enumerated(EnumType.STRING)
    // 테이블 생성 시 이 컬럼에 NOT NULL 제약을 설정함. 기본값을 자동으로 넣는 기능은 아님.
    @Column(nullable = false)
    private JobApplicationStatus status;

    // 회사와 입력값으로 새 지원내역 객체를 만듦. 첫 상태는 APPLIED이며 DB 저장은 별도.
    public static JobApplication createJobApplication(Company company, JobApplicationRequestDto jobApplicationRequestDto) {

        return new JobApplication(null,company,
                jobApplicationRequestDto.getJobTitle(),
                jobApplicationRequestDto.getAppliedAt(),
                jobApplicationRequestDto.getMemo(),
                JobApplicationStatus.APPLIED
        );
    }

    // 이 객체의 직무, 지원 날짜, 메모를 요청값으로 바꿈. ID, 회사 연결, 상태는 유지함.
    public void patch(JobApplicationRequestDto jobApplicationRequestDto) {
        this.jobTitle = jobApplicationRequestDto.getJobTitle();
        this.appliedAt = jobApplicationRequestDto.getAppliedAt();
        this.memo = jobApplicationRequestDto.getMemo();
    }

    // 이 지원내역 객체의 상태만 변경함. DB 저장은 서비스에서 진행함.
    public void changeStatus(JobApplicationStatus status) {
        this.status = status;
    }
}
