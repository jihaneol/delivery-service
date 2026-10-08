package com.example.delivery.order.dto.response;

import com.example.delivery.order.entity.OrderState;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

public class OrderResponse {
    public record Create(
            List<Success> successes,
            List<Failure> failures,
            Integer totalOrderPrice
    ) {

        public record Success(
                Long orderId,
                Long ownerId,
                Integer totalPrice
        ) {}

        public record Failure(
                Long ownerId,
                String reason
        ) {}
    }

    // 주문 목록 조회용 (QueryDSL 어노테이션 기반 프로젝션 -> QOrderResponse_Summary 생성)
    @Getter
    public static class Summary {
        private final Long orderId;
        private final Integer totalPrice;
        private final Integer totalQuantity;
        private final String address;
        private final OrderState orderState;
        private final LocalDateTime createdAt;

        @QueryProjection
        public Summary(Long orderId, Integer totalPrice, Integer totalQuantity,
                       String address, OrderState orderState, LocalDateTime createdAt) {
            this.orderId = orderId;
            this.totalPrice = totalPrice;
            this.totalQuantity = totalQuantity;
            this.address = address;
            this.orderState = orderState;
            this.createdAt = createdAt;
        }
    }

}
