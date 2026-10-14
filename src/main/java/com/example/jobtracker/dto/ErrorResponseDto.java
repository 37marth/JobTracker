package com.example.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    // 사용자에게 알려줄 오류 문장을 담는다.
    private String message;
}