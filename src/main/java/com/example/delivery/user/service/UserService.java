package com.example.delivery.user.service;

import com.example.delivery.global.exception.GlobalException;
import com.example.delivery.global.security.JwtUtil;
import com.example.delivery.user.dto.request.SignupRequest;
import com.example.delivery.user.dto.request.UserRequest;
import com.example.delivery.user.dto.response.LoginResponse;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    public void createUser(SignupRequest signRequest) {
        var loginId = signRequest.getLoginId();

        if(userRepository.existsByLoginId(loginId)) {
            throw new GlobalException.Conflict("이미 사용중인 아이디입니다.");
        }

        var password = signRequest.getPassword();
        var role = signRequest.getRole();

        String encodePw = bCryptPasswordEncoder.encode(password);

        var user = User.builder()
                .loginId(loginId)
                .password(encodePw)
                .role(role)
                .build();

        userRepository.save(user);
    }

    public LoginResponse login(UserRequest userRequest) {
        var loginId = userRequest.getLoginId();
        var password = userRequest.getPassword();

        User user  = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new GlobalException.Unauthorized("아이디 또는 비밀번호가 올바르지 않습니다."));

        if(!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new GlobalException.Unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtUtil.accessToken(user.getLoginId(), user.getRole());

        return new LoginResponse(accessToken);
    }
}
