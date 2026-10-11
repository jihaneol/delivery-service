package com.example.delivery.payment.infrastructure;

import com.example.delivery.payment.domain.Payment;
import java.util.Optional;
import com.example.delivery.payment.domain.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// PaymentRepository 의 JPA 구현
@Repository
@RequiredArgsConstructor
class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    // 주문 취소 시 결제 취소용 (주문 1건당 결제는 1건)
    // "결제 내역 없음"의 의미(409)는 호출하는 쪽이 정하므로 Optional 로 돌려준다
    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return paymentJpaRepository.findByOrderId(orderId);
    }
}
