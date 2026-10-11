package com.example.delivery.user.infrastructure;

import com.example.delivery.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 패키지 밖에서는 보이지 않는다. 같은 패키지의 UserRepositoryImpl 만 사용한다.
interface UserJpaRepository extends JpaRepository<User, Long> {

    boolean existsByLoginId(String loginId);

    Optional<User> findByLoginId(String loginId);
}
