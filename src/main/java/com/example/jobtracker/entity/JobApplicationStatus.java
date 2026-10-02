package com.example.jobtracker.entity;

public enum JobApplicationStatus {
    APPLIED,       // 지원완료
    DOCUMENT_PASS, // 서류합격
    INTERVIEW,     // 면접
    ACCEPTED,      // 최종합격
    REJECTED,      // 불합격
    WITHDRAWN      // 지원취소
}
