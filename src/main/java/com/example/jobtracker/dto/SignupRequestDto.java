package com.example.jobtracker.dto;

import com.example.jobtracker.entity.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SignupRequestDto {
    @NotBlank(message = "아이디를 입력하세요.")
    private String loginId;
    @NotBlank(message = "비밀번호를 입력하세요.")
    private String password;

    public Member toEntity(String encodedPassword) {
        return new Member(null,this.loginId,encodedPassword);
    }
}
