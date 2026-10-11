package com.example.delivery.payment.presentation;

import com.example.delivery.payment.dto.request.PaymentRequest;
import com.example.delivery.payment.dto.response.PaymentResponse;
import com.example.delivery.payment.application.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 결제 - 고객
    @PostMapping
    public ResponseEntity<PaymentResponse.Create> createPayment(
            @AuthenticationPrincipal String loginId,
            @Valid @RequestBody PaymentRequest.Create createRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(loginId, createRequest));
    }
}
