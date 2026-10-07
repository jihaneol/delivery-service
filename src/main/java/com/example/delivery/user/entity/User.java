package com.example.delivery.user.entity;

import com.example.delivery.global.entity.BaseEntity;
import com.example.delivery.user.dto.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 조건에 맞지 않는 생성은 400에러
 */
@Entity(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 아이디 4~20자
    private String loginId;
    // 비밀번호 8자 이상
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}
