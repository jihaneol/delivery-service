package com.example.delivery.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)       // POST, PUT 요청 시 CSRF 방어 비활성화
                .formLogin(AbstractHttpConfigurer::disable)  // 기본 로그인 폼 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)  // HTTP Basic 팝업 인증 비활성화 (401 차단)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()              // 모든 API 요청을 인증 없이 허용
                );

        return http.build();
    }
}
