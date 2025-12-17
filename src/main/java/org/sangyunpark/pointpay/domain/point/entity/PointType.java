package org.sangyunpark.pointpay.domain.point.entity;

public enum PointType {

    CHARGE("충전"),
    USE("사용");

    private final String description;

    PointType(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}