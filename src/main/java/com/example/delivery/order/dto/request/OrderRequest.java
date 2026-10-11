package com.example.delivery.order.dto.request;

import com.example.delivery.order.domain.OrderState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class OrderRequest {
    public record Create(
            @NotEmpty(message = "주문할 메뉴를 선택해주세요.") List<@Valid OrderItem> items,
            @NotBlank(message = "배송지를 입력해주세요.") String shippingAddress
    ) {
    }

    public record OrderItem(
            @NotNull(message = "메뉴를 선택해주세요.") Long menuId,
            @NotNull(message = "수량을 입력해주세요.") @Min(value = 1, message = "수량은 1개 이상이어야 합니다.") Integer quantity
    ) {
        public OrderItem plus(OrderItem orderItem) {
            return new OrderItem(menuId, quantity + orderItem.quantity);
        }
    }

    // 주문 목록 조회 조건 (쿼리 파라미터, 선택). 예: ?states=PAID&states=ACCEPTED
    @Getter
    @AllArgsConstructor
    public static class Search {
        private List<OrderState> states;
    }

    // 주문 상태 변경 (변경할 상태)
    public record ChangeState(
            @NotNull(message = "변경할 주문 상태를 입력해주세요.") OrderState orderState
    ) {
    }
}
