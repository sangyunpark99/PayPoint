package org.sangyunpark.pointpay.point.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PointAddRequest {

    @NotNull
    @Min(1)
    private long userId;

    @NotNull
    @Min(1)
    private int point;
}
