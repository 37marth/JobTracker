package com.example.jobtracker.controller;

import com.example.jobtracker.dto.JobApplicationRequestDto;
import com.example.jobtracker.dto.JobApplicationResponseDto;
import com.example.jobtracker.dto.JobApplicationStatusRequestDto;
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

    // 해당 회사의 지원내역 등록을 서비스에 맡기고, 등록된 DTO를 201 응답으로 보냄.
    @PostMapping("/companies/{companyId}/job-applications")
    // @Valid: 요청 DTO의 @NotBlank, @NotNull을 검사함. 실패하면 메서드 실행 전에 400 응답.
    public ResponseEntity<JobApplicationResponseDto> create(@PathVariable Long companyId, @Valid @RequestBody JobApplicationRequestDto jobApplicationRequestDto) {
        JobApplicationResponseDto jobApplicationResponseDto = jobApplicationService.create(companyId,jobApplicationRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobApplicationResponseDto);
    }

    // 해당 회사의 지원내역 DTO 목록을 서비스에서 받아 200 응답으로 보냄.
    @GetMapping("/companies/{companyId}/job-applications")
    public ResponseEntity<List<JobApplicationResponseDto>> index (@PathVariable Long companyId) {
        List<JobApplicationResponseDto> jobApplicationResponseDto = jobApplicationService.index(companyId);
        return ResponseEntity.status(HttpStatus.OK).body(jobApplicationResponseDto);
    }

    // 지원내역 수정을 서비스에 맡기고, 성공하면 200과 DTO, 대상이 없으면 404를 보냄.
    @PatchMapping("/job-applications/{id}")
    // @Valid: 수정 요청도 DTO의 검증 조건을 검사함. 대상 ID의 존재 여부 검사는 별도.
    public ResponseEntity<JobApplicationResponseDto> update(@PathVariable Long id, @Valid @RequestBody JobApplicationRequestDto jobApplicationRequestDto) {
        JobApplicationResponseDto jobApplicationResponseDto = jobApplicationService.update(id,jobApplicationRequestDto);
        // 없으면 404응답
        if (jobApplicationResponseDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // 수정된 지원내역을 200 OK로 응답한다.
        return ResponseEntity.status(HttpStatus.OK).body(jobApplicationResponseDto);
    }

    // 지원내역 삭제를 서비스에 맡기고, 성공하면 본문 없는 204, 대상이 없으면 404를 보냄.
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

    //지원내역 상태 변경을 요청하는 컨트롤러.
    @PatchMapping("/job-applications/{id}/status")
    public ResponseEntity<JobApplicationResponseDto> updateStatus(@PathVariable Long id, @Valid @RequestBody JobApplicationStatusRequestDto jobApplicationStatusRequestDto) {
        // 1. 서비스에 지원내역 ID와 상태 변경 DTO를 전달한다.
        JobApplicationResponseDto jobApplicationResponseDto =jobApplicationService.updateStatus(id,jobApplicationStatusRequestDto);
        // 2. 서비스 결과가 null이면 404를 응답한다.
        if (jobApplicationResponseDto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // 3. 결과가 있으면 변경된 지원내역 DTO를 200 응답으로 보낸다.
        return ResponseEntity.status(HttpStatus.OK).body(jobApplicationResponseDto);
    }
}
