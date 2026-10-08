package com.example.delivery.order;

import com.example.delivery.global.exception.GlobalException;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.order.dto.request.OrderRequest;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderState;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.order.service.OrderService;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock UserRepository userRepository;
    @Mock MenuRepository menuRepository;
    @Mock OrderRepository orderRepository;
    @InjectMocks OrderService orderService;

    private User user(Long id, String loginId) {
        return User.builder().id(id).loginId(loginId).build();
    }

    private Order order(User customer, User owner, OrderState state) {
        Order order = Order.create(customer, owner, "서울");
        ReflectionTestUtils.setField(order, "orderState", state);
        return order;
    }

    @Test
    void 본인_주문은_취소된다() {
        User customer = user(1L, "c1");
        Order order = order(customer, user(9L, "o1"), OrderState.PENDING_PAYMENT);
        given(userRepository.findByLoginId("c1")).willReturn(Optional.of(customer));
        given(orderRepository.findById(10L)).willReturn(Optional.of(order));

        orderService.cancelOrder("c1", 10L);

        assertThat(order.getOrderState()).isEqualTo(OrderState.CANCELED);
    }

    @Test
    void 다른_손님의_주문은_취소할_수_없다() {
        Order order = order(user(2L, "c2"), user(9L, "o1"), OrderState.PENDING_PAYMENT);
        given(userRepository.findByLoginId("c1")).willReturn(Optional.of(user(1L, "c1")));
        given(orderRepository.findById(10L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder("c1", 10L))
                .isInstanceOf(GlobalException.Forbidden.class);
    }

    @Test
    void 다른_사장님_가게의_주문은_상태를_변경할_수_없다() {
        Order order = order(user(1L, "c1"), user(8L, "o2"), OrderState.PAID);
        given(userRepository.findByLoginId("o1")).willReturn(Optional.of(user(9L, "o1")));
        given(orderRepository.findById(10L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.changeOrderState(
                "o1", 10L, new OrderRequest.ChangeState(OrderState.ACCEPTED)))
                .isInstanceOf(GlobalException.Forbidden.class);
    }

    @Test
    void 없는_메뉴로_주문하면_404() {
        given(userRepository.findByLoginId("c1")).willReturn(Optional.of(user(1L, "c1")));
        given(menuRepository.findAllByIdIn(anyCollection())).willReturn(List.of());

        var request = new OrderRequest.Create(List.of(new OrderRequest.OrderItem(1L, 1)), "서울");

        assertThatThrownBy(() -> orderService.createOrder("c1", request))
                .isInstanceOf(GlobalException.NotFound.class);
    }
}
