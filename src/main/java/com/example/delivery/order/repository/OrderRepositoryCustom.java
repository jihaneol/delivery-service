package com.example.delivery.order.repository;

import com.example.delivery.order.dto.request.OrderRequest;
import com.example.delivery.order.dto.response.OrderResponse;
import com.example.delivery.user.dto.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 조회 전용 (QueryDSL)
public interface OrderRepositoryCustom {
    // 사장님은 받은 주문, 고객은 본인이 한 주문 (상태 조건 + 페이징)
    Page<OrderResponse.Summary> findAllByUser(Long userId, Role role, OrderRequest.Search cond, Pageable pageable);
}
