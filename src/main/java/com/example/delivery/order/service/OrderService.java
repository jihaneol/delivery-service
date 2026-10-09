package com.example.delivery.order.service;

import com.example.delivery.global.exception.GlobalException;
import com.example.delivery.global.exception.GlobalException.NotFound;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.order.dto.request.OrderRequest;
import com.example.delivery.order.dto.response.OrderResponse;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderMenu;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.payment.repository.PaymentRepository;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final OrderProcessor orderProcessor;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    // 트랜잭션을 걸지 않는다.
    public OrderResponse.Create createOrder(String loginId, OrderRequest.Create orderRequest) {

        User customer = findUser(loginId);

        // 한번 전처리 (같은 메뉴는 수량 합산)
        var itemByMenuId = orderRequest.items().stream()
                .collect(Collectors.toMap(
                        OrderRequest.OrderItem::menuId,
                        item -> item,
                        OrderRequest.OrderItem::plus,
                        LinkedHashMap::new));

        List<Menu> menus = menuRepository.findAllByIdIn(itemByMenuId.keySet());

        // 없는 메뉴, 삭제된 메뉴가 하나라도 있으면 전체 404
        if (menus.size() != itemByMenuId.size() || menus.stream().anyMatch(Menu::isDeleted)) {
            throw new NotFound("존재하지 않거나 삭제된 메뉴가 있습니다.");
        }

        // 사장님으로 그룹핑하기 사장님 -> 메뉴들
        Map<Long, List<Menu>> byOwner = new LinkedHashMap<>();
        for (Menu menu : menus) {
            byOwner.computeIfAbsent(menu.getUser().getId(), k -> new ArrayList<>())
                    .add(menu);
        }

        List<OrderResponse.Create.Success> successes = new ArrayList<>();
        List<OrderResponse.Create.Failure> failures = new ArrayList<>();

        // 사장님당 주문은 독립적으로 처리한다. 한 사장님이 실패해도 다른 사장님 주문은 유지된다.
        byOwner.forEach((ownerId, ownerMenus) -> {
            try {
                successes.add(orderProcessor.process(
                        customer, ownerMenus.get(0).getUser(), ownerMenus,
                        itemByMenuId, orderRequest.shippingAddress()));
            } catch (GlobalException e) {
                failures.add(new OrderResponse.Create.Failure(ownerId, e.getMessage()));
            } catch (RuntimeException e) {
                // 사용자에게는 일반 메시지만 주고, 원인은 서버 로그에 남긴다 (마지막 인자 e 가 스택트레이스로 출력됨)
                log.error("주문 처리 실패 customerId={}, ownerId={}", customer.getId(), ownerId, e);
                failures.add(new OrderResponse.Create.Failure(ownerId, "주문 처리 중 오류가 발생했습니다."));
            }
        });

        int totalOrderPrice = successes.stream()
                .mapToInt(OrderResponse.Create.Success::totalPrice)
                .sum();

        return new OrderResponse.Create(successes, failures, totalOrderPrice);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse.Summary> findAllOrder(
            String loginId, OrderRequest.Search cond, Pageable pageable) {
        User user = findUser(loginId);
        return orderRepository.findAllByUser(user.getId(), user.getRole(), cond, pageable);
    }

    // 주문 취소 - 고객(결제 전, 결제 완료) / 사장님(결제 완료, 주문 수락)
    // 결제된 주문이면 결제도 같은 트랜잭션에서 취소한다 (주문 취소 + 결제 취소가 함께 성공하거나 함께 실패)
    @Transactional
    public void cancelOrder(String loginId, Long orderId) {
        User user = findUser(loginId);
        Order order = findOrder(orderId);
        boolean paid = order.isPaid();

        if (order.getCustomer().getId().equals(user.getId())) {
            order.cancelByCustomer();
        } else if (order.getOwner().getId().equals(user.getId())) {
            order.cancelByOwner();
        } else {
            throw new GlobalException.Forbidden("본인의 주문 또는 본인 가게의 주문만 취소할 수 있습니다.");
        }

        if (paid) {
            paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new GlobalException.Conflict("결제 내역이 없어 주문을 취소할 수 없습니다."))
                    .cancel();
        }
    }

    // 재주문 - 고객 : 취소된 주문과 같은 메뉴·수량·배송지로 새 주문(결제 전)을 만든다
    // 취소된 주문과 결제 기록은 그대로 두고, 금액은 현재 메뉴 가격으로 다시 계산한다
    @Transactional
    public OrderResponse.Create.Success reorder(String loginId, Long orderId) {
        User customer = findUser(loginId);
        Order canceledOrder = findOrder(orderId);
        if (!canceledOrder.getCustomer().getId().equals(customer.getId())) {
            throw new GlobalException.Forbidden("본인의 주문만 다시 주문할 수 있습니다.");
        }
        canceledOrder.markReordered();

        Map<Long, Integer> quantityByMenuId = canceledOrder.getOrderMenus().stream()
                .collect(Collectors.toMap(orderMenu -> orderMenu.getMenu().getId(), OrderMenu::getQuantity));
        List<Menu> menus = menuRepository.findAllByIdIn(quantityByMenuId.keySet());

        // 그 사이 삭제된 메뉴가 있으면 재주문하지 않는다 (트랜잭션 롤백으로 재주문 표시도 되돌아간다)
        if (menus.size() != quantityByMenuId.size() || menus.stream().anyMatch(Menu::isDeleted)) {
            throw new NotFound("존재하지 않거나 삭제된 메뉴가 있어 다시 주문할 수 없습니다.");
        }

        Order order = Order.create(customer, canceledOrder.getOwner(), canceledOrder.getAddress());
        for (Menu menu : menus) {
            order.addOrderMenu(menu, quantityByMenuId.get(menu.getId()));
        }
        orderRepository.save(order);

        return new OrderResponse.Create.Success(
                order.getId(), canceledOrder.getOwner().getId(), order.getTotalPrice());
    }

    @Transactional
    public void changeOrderState(String loginId, Long orderId, OrderRequest.ChangeState request) {
        User user = findUser(loginId);
        Order order = findOrder(orderId);
        if (!order.getOwner().getId().equals(user.getId())) {
            throw new GlobalException.Forbidden("본인 가게의 주문만 상태를 변경할 수 있습니다.");
        }
        order.changeState(request.orderState());
    }

    private User findUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NotFound("존재하지 않는 회원입니다."));
    }

    // 상태 변경용 조회라서 주문 행을 잠근다 (동시 요청 직렬화)
    private Order findOrder(Long orderId) {
        return orderRepository.findWithLockById(orderId)
                .orElseThrow(() -> new NotFound("존재하지 않는 주문입니다."));
    }
}
