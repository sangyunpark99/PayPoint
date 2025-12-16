package org.sangyunpark.pointpay.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.order.controller.dto.request.OrderCreateRequest;
import org.sangyunpark.pointpay.domain.order.controller.dto.response.OrderCreateResponse;
import org.sangyunpark.pointpay.domain.order.controller.dto.response.OrderDetailResponse;
import org.sangyunpark.pointpay.domain.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderCreateResponse createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.createOrder(request.getUserId(), request.getItems());
    }

    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrder(@PathVariable long orderId) {
        return orderService.getOrder(orderId);
    }
}
