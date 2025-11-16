package org.sangyunpark.pointpay.domain.point.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PointTransferRequest {

    @NotNull
    private long fromId;

    @NotNull
    private long toId;

    @NotNull
    @Min(1)
    @Max(1_000_000)
    private int pointAmount;

    public long getFromId() {
        return this.fromId;
    }

    public long  getToId() {
        return this.toId;
    }

    public int getPointAmount() {
        return this.pointAmount;
    }
}
