package com.example.jobtracker.entity;

public enum JobApplicationStatus {
    APPLIED,       // 지원완료
    DOCUMENT_PASS, // 서류합격
    INTERVIEW,     // 면접
    ACCEPTED,      // 최종합격
    REJECTED,      // 불합격
    WITHDRAWN;     // 지원취소

    // 화면에 표시할 한글 이름을 반환함. DB와 API에서는 기존 enum 이름을 사용함.
    public String getDisplayName() {
        switch (this) {
            case APPLIED:
                return "지원완료";
            case DOCUMENT_PASS:
                return "서류합격";
            case INTERVIEW:
                return "면접";
            case ACCEPTED:
                return "최종합격";
            case REJECTED:
                return "불합격";
            case WITHDRAWN:
                return "지원취소";
            default:
                return name();
        }
    }
}
