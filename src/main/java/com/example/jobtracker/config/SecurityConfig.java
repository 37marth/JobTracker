package com.example.jobtracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    //그 설정을 반영해서 들어오는 요청에 보안검사 적용
    public SecurityFilterChain securityFilterChain(HttpSecurity http)//어떤요청을 허용할지
            throws Exception {
        //빈 종이 받기 → (다음 단계: 규칙 적기) → build()로 경비원 완성 → @Bean으로 맡기기
                http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/members/signup").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}