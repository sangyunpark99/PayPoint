package org.sangyunpark.pointpay.domain.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.common.idempotency.Idempotent;
import org.sangyunpark.pointpay.domain.payment.controller.dto.request.OrderPaymentRequest;
import org.sangyunpark.pointpay.domain.payment.controller.dto.response.OrderPaymentResponse;
import org.sangyunpark.pointpay.domain.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Idempotent
    @PostMapping("/payment")
    public OrderPaymentResponse orderPayment(
            @Valid @RequestBody OrderPaymentRequest request)
    {
        return paymentService.orderPayment(request.getUserId(), request.getOrderId());
    }
}
