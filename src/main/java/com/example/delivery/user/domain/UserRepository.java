package com.example.delivery.user.domain;

import java.util.Optional;

/**
 * 회원 저장소. 도메인은 이 인터페이스만 알고, JPA 구현(infrastructure)은 모른다.
 */
public interface UserRepository {

    User save(User user);

    boolean existsByLoginId(String loginId);

    // 토큰의 loginId 로 회원 조회. 없으면 404(NotFound)
    User getByLoginId(String loginId);

    // 로그인처럼 "없음"을 404가 아닌 다른 의미로 처리해야 할 때
    Optional<User> findByLoginId(String loginId);
}
