package org.sangyunpark.pointpay.domain.point.entity;

public enum PointType {

    CHARGE("충전"),
    PAYMENT("결제");

    private final String description;

    PointType(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}