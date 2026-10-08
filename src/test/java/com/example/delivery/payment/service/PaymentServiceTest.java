package com.example.delivery.payment.service;

import com.example.delivery.global.exception.GlobalException;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderState;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.payment.dto.request.PaymentRequest;
import com.example.delivery.payment.dto.response.PaymentResponse;
import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentMethod;
import com.example.delivery.payment.repository.PaymentRepository;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock UserRepository userRepository;
    @Mock OrderRepository orderRepository;
    @Mock PaymentRepository paymentRepository;
    @InjectMocks PaymentService paymentService;

    private User user(Long id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Order order(User customer) {
        Order order = Order.create(customer, user(99L), "서울");
        ReflectionTestUtils.setField(order, "totalPrice", 14000);
        return order;
    }

    @Test
    void 결제하면_주문_총액으로_저장되고_주문은_결제완료가_된다() {
        User customer = user(1L);
        Order order = order(customer);
        given(userRepository.findByLoginId("c1")).willReturn(Optional.of(customer));
        given(orderRepository.findById(10L)).willReturn(Optional.of(order));
        given(paymentRepository.save(any(Payment.class))).willAnswer(i -> i.getArgument(0));

        PaymentResponse.Create response =
                paymentService.createPayment("c1", new PaymentRequest.Create(10L, PaymentMethod.CARD));

        assertThat(response.amount()).isEqualTo(14000);
        assertThat(order.getOrderState()).isEqualTo(OrderState.PAID);
    }

    @Test
    void 다른_손님의_주문은_결제할_수_없다() {
        Order order = order(user(2L));
        given(userRepository.findByLoginId("c1")).willReturn(Optional.of(user(1L)));
        given(orderRepository.findById(10L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.createPayment("c1", new PaymentRequest.Create(10L, PaymentMethod.CARD)))
                .isInstanceOf(GlobalException.Forbidden.class);
        verify(paymentRepository, never()).save(any());
    }
}
