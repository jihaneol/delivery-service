package com.example.delivery.payment.repository;

import com.example.delivery.payment.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 주문 취소 시 결제 취소용 (주문 1건당 결제는 1건)
    Optional<Payment> findByOrderId(Long orderId);
}
