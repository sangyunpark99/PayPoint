package org.sangyunpark.pointpay.domain.order.controller.dto.response;

import lombok.Builder;
import org.sangyunpark.pointpay.domain.order.entity.Order;
import org.sangyunpark.pointpay.domain.order.entity.OrderStatus;

public class OrderCreateResponse {

    private long orderId;
    private OrderStatus status;
    private long totalAmount;

    @Builder
    public OrderCreateResponse(long orderId, OrderStatus status, long totalAmount) {
        this.orderId = orderId;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public long getOrderId() {
        return this.orderId;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public long getTotalAmount() {
        return this.totalAmount;
    }

    public static OrderCreateResponse fromOrder(Order order) {
        return OrderCreateResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .build();
    }
}
