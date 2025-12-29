package org.sangyunpark.pointpay.domain.payment.controller.dto.request;

import jakarta.validation.constraints.NotNull;

public class OrderPaymentRequest {

    @NotNull
    private long orderId;

    @NotNull
    private long userId;

    protected OrderPaymentRequest() {

    }

    public OrderPaymentRequest(long orderId, long userId) {
        this.orderId = orderId;
        this.userId = userId;
    }

    public long getOrderId() {
        return this.orderId;
    }

    public long getUserId() {
        return this.userId;
    }
}
