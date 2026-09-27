package com.example.jobtracker.controller;

import com.example.jobtracker.dto.JobApplicationRequestDto;
import com.example.jobtracker.entity.JobApplication;
import com.example.jobtracker.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class JobApplicationApiController {

    @Autowired
    private JobApplicationService jobApplicationService;

    @PostMapping("/companies/{companyId}/job-applications")
    public ResponseEntity<JobApplication> create( @PathVariable Long companyId,@Valid @RequestBody JobApplicationRequestDto jobApplicationRequestDto) {
        JobApplication jobApplication = jobApplicationService.create(companyId,jobApplicationRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobApplication);
    }

    @GetMapping("/companies/{companyId}/job-applications")
    public ResponseEntity<List<JobApplication>> index (@PathVariable Long companyId) {
        List<JobApplication> jobApplications = jobApplicationService.index(companyId);
        return ResponseEntity.status(HttpStatus.OK).body(jobApplications);
    }

    @PatchMapping("/job-applications/{id}")
    public ResponseEntity<JobApplication> update(@PathVariable Long id, @Valid @RequestBody JobApplicationRequestDto jobApplicationRequestDto) {
        // 1. 주소에서 수정할 지원내역 ID를 받는다.
        // 2. 요청 본문에서 수정할 값들을 DTO로 받는다.
        // 3. ID와 DTO를 서비스의 update 메서드에 전달한다.
        JobApplication jobApplication = jobApplicationService.update(id,jobApplicationRequestDto);
        // 없으면 404응답
        if (jobApplication == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // 4. 수정된 지원내역을 200 OK로 응답한다.
        return ResponseEntity.status(HttpStatus.OK).body(jobApplication);
    }
}
