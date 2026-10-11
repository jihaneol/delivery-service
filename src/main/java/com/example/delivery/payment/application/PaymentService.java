package com.example.delivery.payment.application;

import com.example.delivery.order.domain.Order;
import com.example.delivery.order.domain.OrderRepository;
import com.example.delivery.payment.dto.request.PaymentRequest;
import com.example.delivery.payment.dto.response.PaymentResponse;
import com.example.delivery.payment.domain.Payment;
import com.example.delivery.payment.domain.PaymentRepository;
import com.example.delivery.user.domain.User;
import com.example.delivery.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.delivery.global.exception.GlobalException.Forbidden;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public PaymentResponse.Create createPayment(String loginId, PaymentRequest.Create createRequest) {
        User user = userRepository.getByLoginId(loginId);
        Order order = orderRepository.getForUpdate(createRequest.orderId());

        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new Forbidden("본인의 주문만 결제할 수 있습니다.");
        }

        order.pay();
        Payment payment = paymentRepository.save(Payment.create(order, createRequest.paymentMethod()));

        return new PaymentResponse.Create(payment.getId(), order.getId(), payment.getAmount());
    }
}
