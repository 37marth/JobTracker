package com.example.jobtracker.exception;

import com.example.jobtracker.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 여러 컨트롤러의 예외를 함께 처리하고, 처리 결과를 응답 본문으로 보낸다.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid 검증에 실패하면 Spring이 이 메서드에 예외 객체를 전달한다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 검증 실패 이유 하나를 오류 DTO에 담아 400 응답으로 반환한다.
    public ResponseEntity<ErrorResponseDto> handleValidation(
            MethodArgumentNotValidException exception) {

        // 1. 검증 결과에서 오류가 있는 필드 하나를 찾는다.
        FieldError fieldError = exception.getBindingResult().getFieldError();

        // 2. @NotBlank, @NotNull 등에 적은 message를 가져온다.
        // 필드 오류나 문구가 없을 때는 기본 안내 문장을 사용한다.
        String message = "입력값을 확인해 주세요.";
        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            message = fieldError.getDefaultMessage();
        }

        // 3. 사용자에게 보낼 오류 문장을 응답 DTO에 담는다.
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(message);

        // 4. 잘못된 입력이므로 400과 오류 DTO를 함께 반환한다.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }

    // 회사 이름이 중복되면 이 메서드가 예외를 받아 처리한다.
    @ExceptionHandler(DuplicateCompanyException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateCompany(
            DuplicateCompanyException exception) {

        // 서비스에서 예외에 담아 보낸 메시지를 꺼낸다.
        String message = exception.getMessage();

        // 메시지를 오류 응답 DTO에 담는다.
        ErrorResponseDto errorResponseDto = new ErrorResponseDto(message);

        // 기존 회사와 중복되므로 409 응답과 오류 메시지를 보낸다.
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponseDto);
    }
}
