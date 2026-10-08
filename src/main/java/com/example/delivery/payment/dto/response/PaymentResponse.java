package com.example.delivery.payment.dto.response;

public class PaymentResponse {
    public record Create(Long paymentId, Long orderId, Integer amount) {
    }
}
