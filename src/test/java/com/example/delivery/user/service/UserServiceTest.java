package com.example.delivery.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    void getUserById() {
        String pw = "user1234";
        String pw2 = "user1234";

        String encode = bCryptPasswordEncoder.encode(pw);
        boolean matches = bCryptPasswordEncoder.matches(pw2, encode);

        assertEquals(matches, true);
    }

}