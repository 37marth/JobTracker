package com.example.jobtracker.controller;

import com.example.jobtracker.dto.JobApplicationRequestDto;
import com.example.jobtracker.dto.JobApplicationResponseDto;
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
    public ResponseEntity<JobApplicationResponseDto> create(@PathVariable Long companyId, @Valid @RequestBody JobApplicationRequestDto jobApplicationRequestDto) {
        JobApplicationResponseDto jobApplicationResponseDto = jobApplicationService.create(companyId,jobApplicationRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobApplicationResponseDto);
    }

    @GetMapping("/companies/{companyId}/job-applications")
    public ResponseEntity<List<JobApplicationResponseDto>> index (@PathVariable Long companyId) {
        List<JobApplicationResponseDto> jobApplicationResponseDto = jobApplicationService.index(companyId);
        return ResponseEntity.status(HttpStatus.OK).body(jobApplicationResponseDto);
    }

    @PatchMapping("/job-applications/{id}")
    public ResponseEntity<JobApplicationResponseDto> update(@PathVariable Long id, @Valid @RequestBody JobApplicationRequestDto jobApplicationRequestDto) {
        JobApplicationResponseDto jobApplicationResponseDto = jobApplicationService.update(id,jobApplicationRequestDto);
        // 없으면 404응답
        if (jobApplicationResponseDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // 4. 수정된 지원내역을 200 OK로 응답한다.
        return ResponseEntity.status(HttpStatus.OK).body(jobApplicationResponseDto);
    }

    @DeleteMapping("/job-applications/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // 1. 서비스에 이 ID의 지원내역 삭제를 요청한다.
        JobApplication jobApplication = jobApplicationService.delete(id);
        // 2. 삭제할 지원내역이 없으면 404를 응답한다.
        if (jobApplication == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // 3. 삭제했으면 204를 응답한다.
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
