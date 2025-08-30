package org.sangyunpark.pointpay.point;

public enum PointStatus {

    EARN("충전"),
    USE("결제");

    private final String description;

    PointStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}