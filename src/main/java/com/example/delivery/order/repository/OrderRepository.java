package com.example.delivery.order.repository;

import com.example.delivery.order.entity.Order;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    // 상태를 바꾸는 요청(결제, 취소, 상태 변경, 재주문)은 주문 행을 잠그고 가져온다 (SELECT ... FOR UPDATE)
    // 동시에 들어온 요청은 앞 트랜잭션이 끝날 때까지 기다린 뒤, 바뀐 상태를 보고 검증한다
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Order> findWithLockById(Long id);
}
