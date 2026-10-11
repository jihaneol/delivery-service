package com.example.delivery.payment.domain;

import java.util.Optional;

/**
 * 결제 저장소.
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    // 주문 1건당 결제는 1건. "결제 내역 없음"의 의미(409 등)는 호출하는 쪽이 정한다
    Optional<Payment> findByOrderId(Long orderId);
}
