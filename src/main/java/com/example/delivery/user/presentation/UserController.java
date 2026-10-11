package com.example.delivery.user.presentation;

import com.example.delivery.user.dto.request.SignupRequest;
import com.example.delivery.user.dto.request.UserRequest;
import com.example.delivery.user.dto.response.LoginResponse;
import com.example.delivery.user.application.UserService;
import jakarta.validation.Valid;
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

    @PostMapping("/signup")
    public void signup(@Valid @RequestBody SignupRequest signRequest) {
        userService.createUser(signRequest);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody UserRequest userRequest) {
        return userService.login(userRequest);
    }
}
