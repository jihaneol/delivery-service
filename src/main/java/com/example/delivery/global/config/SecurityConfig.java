package com.example.delivery.global.config;

import com.example.delivery.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 사용, 세션 미사용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/signup", "/user/login").permitAll() // 회원가입, 로그인은 공개
                        .requestMatchers(HttpMethod.GET, "/menu/**").permitAll()    // 메뉴 조회는 누구나
                        .requestMatchers("/menu/**").hasRole("OWNER")               // 메뉴 등록/수정/삭제는 OWNER만
                        .requestMatchers(HttpMethod.POST, "/order").hasRole("CUSTOMER")          // 주문 생성은 CUSTOMER만
                        .requestMatchers(HttpMethod.PATCH, "/order/*/cancel").hasRole("CUSTOMER") // 주문 취소는 CUSTOMER만
                        .requestMatchers(HttpMethod.PATCH, "/order/*/state").hasRole("OWNER")     // 상태 변경은 OWNER만
                        .requestMatchers(HttpMethod.POST, "/payment").hasRole("CUSTOMER")         // 결제는 CUSTOMER만
                        .requestMatchers("/order/**").authenticated()                            // 그 외 주문 API는 로그인 필요
                        .anyRequest().permitAll()                               // 나머지는 인증 필요
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // 인증 실패 401
                        .accessDeniedHandler((request, response, e) ->
                                response.setStatus(HttpStatus.FORBIDDEN.value())))                   // 권한 부족 403
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
