package com.example.delivery.payment.infrastructure;

import com.example.delivery.payment.domain.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 패키지 밖에서는 보이지 않는다. 같은 패키지의 PaymentRepositoryImpl 만 사용한다.
interface PaymentJpaRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);
}
