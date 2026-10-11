package com.example.delivery.order.domain;

/**
 * 주문 저장소 (변경용). Order 애그리거트(OrderMenu 포함)를 저장하고 꺼낸다.
 * 화면용 조회는 infrastructure.OrderQueryRepository 가 맡는다.
 */
public interface OrderRepository {

    Order save(Order order);

    // 상태를 바꾸려고(결제, 취소, 상태 변경, 재주문) 가져오는 주문. 없으면 404(NotFound)
    // 동시에 들어온 변경 요청이 서로 덮어쓰지 않도록 구현체가 잠금을 보장해야 한다.
    Order getForUpdate(Long orderId);
}
