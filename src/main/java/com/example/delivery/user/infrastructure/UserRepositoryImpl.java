package com.example.delivery.user.infrastructure;

import com.example.delivery.global.exception.GlobalException.NotFound;
import com.example.delivery.user.domain.User;
import java.util.Optional;
import com.example.delivery.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// UserRepository 의 JPA 구현. 같은 패키지의 UserJpaRepository 로 동작한다
@Repository
@RequiredArgsConstructor
class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        return userJpaRepository.existsByLoginId(loginId);
    }

    // 토큰의 loginId 로 회원 조회. 없으면 404
    @Override
    public User getByLoginId(String loginId) {
        return userJpaRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NotFound("존재하지 않는 회원입니다."));
    }

    // 로그인처럼 "없음"을 404가 아닌 다른 의미로 처리해야 할 때
    @Override
    public Optional<User> findByLoginId(String loginId) {
        return userJpaRepository.findByLoginId(loginId);
    }
}
