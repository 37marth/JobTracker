package com.example.jobtracker.controller;

import com.example.jobtracker.dto.JobApplicationRequestDto;
import com.example.jobtracker.entity.JobApplication;
import com.example.jobtracker.service.JobApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class JobApplicationApiController {

    @Autowired
    private JobApplicationService jobApplicationService;

    @PostMapping("/companies/{companyId}/job-applications")
    public ResponseEntity<JobApplication> create(@PathVariable Long companyId, @RequestBody JobApplicationRequestDto jobApplicationRequestDto) {
        JobApplication jobApplication = jobApplicationService.create(companyId,jobApplicationRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobApplication);
    }

}
