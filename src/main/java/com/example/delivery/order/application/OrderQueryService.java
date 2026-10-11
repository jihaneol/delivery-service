package com.example.delivery.order.application;

import com.example.delivery.order.dto.request.OrderRequest;
import com.example.delivery.order.dto.response.OrderResponse;
import com.example.delivery.order.infrastructure.OrderQueryRepository;
import com.example.delivery.user.domain.User;
import com.example.delivery.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 조회 (Query). 상태를 바꾸지 않고 DTO 만 돌려준다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryService {

    private final UserRepository userRepository;
    private final OrderQueryRepository orderQueryRepository;

    // 주문 목록 - 고객은 본인 주문, 사장님은 받은 주문
    public Page<OrderResponse.Summary> findAllOrder(
            String loginId, OrderRequest.Search cond, Pageable pageable) {
        User user = userRepository.getByLoginId(loginId);
        return orderQueryRepository.findAllByUser(user.getId(), user.getRole(), cond, pageable);
    }
}
