package org.sangyunpark.pointpay.domain.payment.controller.dto.response;

import lombok.Builder;

public class OrderPaymentResponse {

    private long userId;
    private long orderId;
    private long pointBalance;
    private long beforePointBalance;

    @Builder
    public OrderPaymentResponse(long userId, long orderId, long pointBalance, long beforePointBalance) {
        this.userId = userId;
        this.orderId = orderId;
        this.pointBalance = pointBalance;
        this.beforePointBalance = beforePointBalance;
    }

    public long getUserId() {
        return this.userId;
    }

    public long getOrderId() {
        return this.orderId;
    }

    public long getPointBalance() {
        return this.pointBalance;
    }

    public long getBeforePointBalance() {
        return this.beforePointBalance;
    }
}
