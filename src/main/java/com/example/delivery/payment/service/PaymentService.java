package com.example.delivery.payment.service;

import com.example.delivery.order.entity.Order;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.payment.dto.request.PaymentRequest;
import com.example.delivery.payment.dto.response.PaymentResponse;
import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.repository.PaymentRepository;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.delivery.global.exception.GlobalException.Forbidden;
import static com.example.delivery.global.exception.GlobalException.NotFound;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public PaymentResponse.Create createPayment(String loginId, PaymentRequest.Create createRequest) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NotFound("존재하지 않는 회원입니다."));
        Order order = orderRepository.findById(createRequest.orderId())
                .orElseThrow(() -> new NotFound("존재하지 않는 주문입니다."));

        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new Forbidden("본인의 주문만 결제할 수 있습니다.");
        }

        order.pay();
        Payment payment = paymentRepository.save(Payment.create(order, createRequest.paymentMethod()));

        return new PaymentResponse.Create(payment.getId(), order.getId(), payment.getAmount());
    }
}
