package com.example.delivery.user.controller;

import com.example.delivery.user.dto.request.SignupRequest;
import com.example.delivery.user.dto.request.UserRequest;
import com.example.delivery.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    // 회원 가입
    // 아이디는 4~20자, 비밀번호는 8자 이상입니다
    @PostMapping("/signup")
    public void signup(@RequestBody SignupRequest signRequest) {
        userService.createUser(signRequest);
    }


    // 로그인 + 토큰 생성
    @PostMapping("/login")
    public String login(@RequestBody UserRequest userRequest) {

        return userService.login(userRequest);
    }

}
