package com.example.delivery.order.application;

import com.example.delivery.global.exception.GlobalException;
import com.example.delivery.menu.domain.Menu;
import com.example.delivery.menu.domain.MenuRepository;
import com.example.delivery.order.dto.request.OrderRequest;
import com.example.delivery.order.domain.Order;
import com.example.delivery.order.domain.OrderState;
import com.example.delivery.order.domain.OrderRepository;
import com.example.delivery.payment.domain.Payment;
import com.example.delivery.payment.domain.PaymentMethod;
import com.example.delivery.payment.domain.PaymentState;
import com.example.delivery.payment.domain.PaymentRepository;
import com.example.delivery.user.domain.User;
import com.example.delivery.user.domain.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock UserRepository userRepository;
    @Mock MenuRepository menuRepository;
    @Mock OrderRepository orderRepository;
    @Mock PaymentRepository paymentRepository;
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
        given(userRepository.getByLoginId("c1")).willReturn(customer);
        given(orderRepository.getForUpdate(10L)).willReturn(order);

        orderService.cancelOrder("c1", 10L);

        assertThat(order.getOrderState()).isEqualTo(OrderState.CANCELED);
    }

    @Test
    void 결제_전_주문을_취소하면_결제_취소는_하지_않는다() {
        User customer = user(1L, "c1");
        Order order = order(customer, user(9L, "o1"), OrderState.PENDING_PAYMENT);
        given(userRepository.getByLoginId("c1")).willReturn(customer);
        given(orderRepository.getForUpdate(10L)).willReturn(order);

        orderService.cancelOrder("c1", 10L);

        verify(paymentRepository, never()).findByOrderId(any());
    }

    @Test
    void 고객이_결제된_주문을_취소하면_결제도_취소된다() {
        User customer = user(1L, "c1");
        Order order = order(customer, user(9L, "o1"), OrderState.PAID);
        Payment payment = Payment.create(order, PaymentMethod.CARD);
        given(userRepository.getByLoginId("c1")).willReturn(customer);
        given(orderRepository.getForUpdate(10L)).willReturn(order);
        given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

        orderService.cancelOrder("c1", 10L);

        assertThat(order.getOrderState()).isEqualTo(OrderState.CANCELED);
        assertThat(payment.getPaymentState()).isEqualTo(PaymentState.CANCELED);
    }

    @Test
    void 사장님이_수락한_주문을_취소하면_결제도_취소된다() {
        User owner = user(9L, "o1");
        Order order = order(user(1L, "c1"), owner, OrderState.ACCEPTED);
        Payment payment = Payment.create(order, PaymentMethod.CARD);
        given(userRepository.getByLoginId("o1")).willReturn(owner);
        given(orderRepository.getForUpdate(10L)).willReturn(order);
        given(paymentRepository.findByOrderId(10L)).willReturn(Optional.of(payment));

        orderService.cancelOrder("o1", 10L);

        assertThat(order.getOrderState()).isEqualTo(OrderState.CANCELED);
        assertThat(payment.getPaymentState()).isEqualTo(PaymentState.CANCELED);
    }

    @Test
    void 결제_내역이_없으면_결제된_주문은_취소되지_않는다() {
        User customer = user(1L, "c1");
        Order order = order(customer, user(9L, "o1"), OrderState.PAID);
        given(userRepository.getByLoginId("c1")).willReturn(customer);
        given(orderRepository.getForUpdate(10L)).willReturn(order);
        given(paymentRepository.findByOrderId(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder("c1", 10L))
                .isInstanceOf(GlobalException.Conflict.class);
    }

    @Test
    void 다른_가게_사장님은_주문을_취소할_수_없다() {
        Order order = order(user(1L, "c1"), user(8L, "o2"), OrderState.PAID);
        given(userRepository.getByLoginId("o1")).willReturn(user(9L, "o1"));
        given(orderRepository.getForUpdate(10L)).willReturn(order);

        assertThatThrownBy(() -> orderService.cancelOrder("o1", 10L))
                .isInstanceOf(GlobalException.Forbidden.class);
    }

    private Menu menu(Long id, int price) {
        Menu menu = Menu.create("메뉴" + id, price, null, null);
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }

    @Test
    void 취소된_주문을_재주문하면_현재_가격으로_새_주문이_만들어진다() {
        User customer = user(1L, "c1");
        Menu menu = menu(1L, 7000);
        Order canceled = order(customer, user(9L, "o1"), OrderState.CANCELED);
        canceled.addOrderMenu(menu, 2);
        menu.update(null, 8000, null); // 취소 후 가격 인상
        given(userRepository.getByLoginId("c1")).willReturn(customer);
        given(orderRepository.getForUpdate(10L)).willReturn(canceled);
        given(menuRepository.getActiveMenus(anyCollection())).willReturn(List.of(menu));

        var response = orderService.reorder("c1", 10L);

        assertThat(response.totalPrice()).isEqualTo(16000);
        assertThat(canceled.isReordered()).isTrue();
        assertThat(canceled.getOrderState()).isEqualTo(OrderState.CANCELED);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void 삭제된_메뉴가_있으면_재주문할_수_없다() {
        User customer = user(1L, "c1");
        Order canceled = order(customer, user(9L, "o1"), OrderState.CANCELED);
        canceled.addOrderMenu(menu(1L, 7000), 1);
        given(userRepository.getByLoginId("c1")).willReturn(customer);
        given(orderRepository.getForUpdate(10L)).willReturn(canceled);
        given(menuRepository.getActiveMenus(anyCollection()))
                .willThrow(new GlobalException.NotFound("존재하지 않거나 삭제된 메뉴가 있습니다."));

        assertThatThrownBy(() -> orderService.reorder("c1", 10L))
                .isInstanceOf(GlobalException.NotFound.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void 다른_손님의_주문은_재주문할_수_없다() {
        Order canceled = order(user(2L, "c2"), user(9L, "o1"), OrderState.CANCELED);
        given(userRepository.getByLoginId("c1")).willReturn(user(1L, "c1"));
        given(orderRepository.getForUpdate(10L)).willReturn(canceled);

        assertThatThrownBy(() -> orderService.reorder("c1", 10L))
                .isInstanceOf(GlobalException.Forbidden.class);
    }

    @Test
    void 다른_손님의_주문은_취소할_수_없다() {
        Order order = order(user(2L, "c2"), user(9L, "o1"), OrderState.PENDING_PAYMENT);
        given(userRepository.getByLoginId("c1")).willReturn(user(1L, "c1"));
        given(orderRepository.getForUpdate(10L)).willReturn(order);

        assertThatThrownBy(() -> orderService.cancelOrder("c1", 10L))
                .isInstanceOf(GlobalException.Forbidden.class);
    }

    @Test
    void 다른_사장님_가게의_주문은_상태를_변경할_수_없다() {
        Order order = order(user(1L, "c1"), user(8L, "o2"), OrderState.PAID);
        given(userRepository.getByLoginId("o1")).willReturn(user(9L, "o1"));
        given(orderRepository.getForUpdate(10L)).willReturn(order);

        assertThatThrownBy(() -> orderService.changeOrderState(
                "o1", 10L, new OrderRequest.ChangeState(OrderState.ACCEPTED)))
                .isInstanceOf(GlobalException.Forbidden.class);
    }

    @Test
    void 없는_메뉴로_주문하면_404() {
        given(userRepository.getByLoginId("c1")).willReturn(user(1L, "c1"));
        given(menuRepository.getActiveMenus(anyCollection()))
                .willThrow(new GlobalException.NotFound("존재하지 않거나 삭제된 메뉴가 있습니다."));

        var request = new OrderRequest.Create(List.of(new OrderRequest.OrderItem(1L, 1)), "서울");

        assertThatThrownBy(() -> orderService.createOrder("c1", request))
                .isInstanceOf(GlobalException.NotFound.class);
    }
}
