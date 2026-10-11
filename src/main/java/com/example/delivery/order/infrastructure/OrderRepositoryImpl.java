package com.example.delivery.order.infrastructure;

import com.example.delivery.global.exception.GlobalException.NotFound;
import com.example.delivery.order.domain.Order;
import com.example.delivery.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// OrderRepository 의 JPA 구현. getForUpdate 는 비관적 락(SELECT ... FOR UPDATE)으로 주문 행을 잠근다
@Repository
@RequiredArgsConstructor
class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    // 상태를 바꾸려고(결제, 취소, 상태 변경, 재주문) 가져오는 주문. 없으면 404
    // 주문 행을 잠그므로 동시에 들어온 요청은 앞 트랜잭션이 끝난 뒤 바뀐 상태로 검증된다.
    // 잠금 방식을 바꾸더라도 서비스는 그대로 두고 이 메서드만 고치면 된다.
    @Override
    public Order getForUpdate(Long orderId) {
        return orderJpaRepository.findWithLockById(orderId)
                .orElseThrow(() -> new NotFound("존재하지 않는 주문입니다."));
    }
}
