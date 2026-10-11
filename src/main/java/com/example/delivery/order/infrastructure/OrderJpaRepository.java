package com.example.delivery.order.infrastructure;

import com.example.delivery.order.domain.Order;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

// 패키지 밖에서는 보이지 않는다. 같은 패키지의 OrderRepositoryImpl 만 사용한다.
interface OrderJpaRepository extends JpaRepository<Order, Long> {

    // SELECT ... FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Order> findWithLockById(Long id);
}
