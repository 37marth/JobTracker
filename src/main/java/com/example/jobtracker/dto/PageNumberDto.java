package com.example.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageNumberDto {

    // 화면에 표시할 페이지 번호: 1부터 시작.
    private int displayNumber;

    // 이동 주소에 넣을 페이지 번호: 0부터 시작.
    private int requestPageNumber;
}