package org.sangyunpark.pointpay.domain.point.dto.response;

public class PointBalanceResponse {

    private long userId;
    private int pointBalance;

    public PointBalanceResponse(final long userId, final int pointBalance) {
        this.userId = userId;
        this.pointBalance = pointBalance;
    }

    public long getUserId() {
        return userId;
    }

    public int getPointBalance() {
        return pointBalance;
    }
}
