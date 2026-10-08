package com.example.delivery.order.entity;

public enum OrderState {
    PENDING_PAYMENT,   // 결제 전 (결제 대기)
    PAID,              // 결제 완료
    ACCEPTED,          // 주문 수락 (사장님 접수)
    DELIVERED,         // 배달 완료
    CANCELED           // 주문 취소
}
