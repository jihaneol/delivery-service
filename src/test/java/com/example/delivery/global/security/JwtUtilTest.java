package com.example.delivery.global.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.delivery.user.dto.enums.Role;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-for-jwt-must-be-32-bytes-or-more";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000);

    @Test
    void 토큰에_loginId와_role이_담긴다() {
        String token = jwtUtil.accessToken("user1234", Role.OWNER);

        assertTrue(jwtUtil.validate(token));
        assertEquals("user1234", jwtUtil.getLoginId(token));
        assertEquals(Role.OWNER, jwtUtil.getRole(token));
    }

    @Test
    void 변조된_토큰은_검증에_실패한다() {
        String token = jwtUtil.accessToken("user1234", Role.CUSTOMER);

        assertFalse(jwtUtil.validate(token + "x"));
        assertFalse(jwtUtil.validate("not-a-jwt"));
    }

    @Test
    void 만료된_토큰은_검증에_실패한다() {
        JwtUtil expired = new JwtUtil(SECRET, -1000);

        assertFalse(jwtUtil.validate(expired.accessToken("user1234", Role.CUSTOMER)));
    }

    @Test
    void 다른_키로_서명된_토큰은_검증에_실패한다() {
        JwtUtil other = new JwtUtil("another-secret-key-for-jwt-must-be-32-bytes-long", 60_000);

        assertFalse(jwtUtil.validate(other.accessToken("user1234", Role.CUSTOMER)));
    }
}
