package com.example.delivery.payment.dto.request;

import com.example.delivery.payment.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {
    // 결제 금액은 받지 않는다 (주문 총액을 서버가 사용)
    public record Create(
            @NotNull(message = "주문을 선택해주세요.") Long orderId,
            @NotNull(message = "카드 결제만 가능합니다.") PaymentMethod paymentMethod
    ) {
    }
}
