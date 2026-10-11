package com.example.delivery.order.presentation;

import com.example.delivery.order.dto.request.OrderRequest;
import com.example.delivery.order.dto.response.OrderResponse;
import com.example.delivery.order.application.OrderService;
import com.example.delivery.order.application.OrderQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderQueryService orderQueryService;

    /**
     * 주문 생성
     * 해당 가게 사장님1 - 팔보체 2개, 짜장면 1개
     * 해당 가게 사장님2 - 팥빙수 1개
     */
    @PostMapping
    public ResponseEntity<OrderResponse.Create> createOrder(
            @AuthenticationPrincipal String loginId,
            @Valid @RequestBody OrderRequest.Create orderRequest
    ) {
        // 성공/실패는 본문으로 구분하므로 항상 201
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(loginId, orderRequest));
    }

    /**
     * 주문 목록 조회
     * 사장님 - 받은 주문만 나온다.
     * 고객 - 본인의 주문이 들어온다.
     */
    @GetMapping
    public PagedModel<OrderResponse.Summary> findAllOrder(
            @AuthenticationPrincipal String loginId,
            OrderRequest.Search condition,
            Pageable pageable
    ) {
        return new PagedModel<>(orderQueryService.findAllOrder(loginId, condition, pageable));
    }

    /**
     * 주문 취소
     * 고객 - 결제 전, 결제 완료(사장님 수락 전)
     * 사장님 - 결제 완료, 주문 수락 (주문 거절)
     * 결제된 주문은 결제도 함께 취소된다
     */
    @PatchMapping("/{id}/cancel")
    public void cancelOrder(
            @AuthenticationPrincipal String loginId,
            @PathVariable("id") Long orderId
    ) {
        orderService.cancelOrder(loginId, orderId);
    }

    /**
     * 재주문 - 고객
     * 취소된 주문과 같은 메뉴·수량·배송지로 새 주문을 만든다 (주문 1건당 한 번)
     */
    @PostMapping("/{id}/reorder")
    public ResponseEntity<OrderResponse.Create.Success> reorder(
            @AuthenticationPrincipal String loginId,
            @PathVariable("id") Long orderId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.reorder(loginId, orderId));
    }

    /**
     * 주문 상태 변경
     * 사장님만 가능
     * 결제완료 → 주문수락, 주문수락 → 배달완료 두 가지만 허용
     */
    @PatchMapping("/{id}/state")
    public void changeOrderState(
            @AuthenticationPrincipal String loginId,
            @PathVariable("id") Long orderId,
            @Valid @RequestBody OrderRequest.ChangeState request
    ) {
        orderService.changeOrderState(loginId, orderId, request);
    }

}
