package com.example.delivery.order.service;

import com.example.delivery.menu.entity.Menu;
import com.example.delivery.order.dto.request.OrderRequest;
import com.example.delivery.order.dto.response.OrderResponse;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.user.entity.User;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사장님 한 명 분량의 주문을 하나의 트랜잭션으로 저장한다.
 * 검증은 OrderService 에서 끝났으므로 여기서는 저장만 한다.
 * OrderService 와 다른 빈이어야 @Transactional 프록시가 적용된다.
 */
@Service
@RequiredArgsConstructor
public class OrderProcessor {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse.Create.Success process(
            User customer, User owner, List<Menu> menus,
            Map<Long, OrderRequest.OrderItem> itemByMenuId, String address) {

        Order order = Order.create(customer, owner, address);
        for (Menu menu : menus) {
            order.addOrderMenu(menu, itemByMenuId.get(menu.getId()).quantity());
        }
        orderRepository.save(order);

        return new OrderResponse.Create.Success(order.getId(), owner.getId(), order.getTotalPrice());
    }
}
