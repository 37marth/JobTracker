package com.example.jobtracker.exception;

// 같은 이름의 회사가 이미 등록되어 있을 때 사용하는 예외.
public class DuplicateCompanyException extends RuntimeException {
    public DuplicateCompanyException(String message) {
        super(message);
    }
}