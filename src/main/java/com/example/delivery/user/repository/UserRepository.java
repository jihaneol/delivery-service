package com.example.delivery.user.repository;

import com.example.delivery.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginIdAndPassword(String loginId, String password);

    boolean existsByLoginIdAndPassword(String loginId, String password);

    boolean existsByLoginId(String loginId);

    Optional<User> findByLoginId(String loginId);
}
