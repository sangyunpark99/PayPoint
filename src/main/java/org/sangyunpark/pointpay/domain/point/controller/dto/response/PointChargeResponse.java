package org.sangyunpark.pointpay.domain.point.controller.dto.response;

import lombok.Builder;

public class PointChargeResponse {

    private long userId;
    private int chargePoint;
    private int currentPoint;

    @Builder
    public PointChargeResponse(long userId, int chargePoint, int currentPoint) {
        this.userId = userId;
        this.chargePoint = chargePoint;
        this.currentPoint = currentPoint;
    }

    public long getUserId() {
        return this.userId;
    }

    public int getChargePoint() {
        return this.chargePoint;
    }

    public int getCurrentPoint() {
        return this.currentPoint;
    }
}
