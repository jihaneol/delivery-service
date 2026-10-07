package com.example.delivery.user.dto.request;

import com.example.delivery.user.dto.enums.Role;
import lombok.Getter;

@Getter
public class SignupRequest{
    private String loginId;
    private String password;
    private Role role;
}
