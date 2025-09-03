package org.sangyunpark.pointpay.domain.point.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PointAddRequest {

    @NotNull
    @Min(1)
    private long userId;

    @NotNull
    @Min(1)
    @Max(1_000_000)
    private int point;

    public long getUserId() {
        return this.userId;
    }

    public int getPoint() {
        return this.point;
    }
}
