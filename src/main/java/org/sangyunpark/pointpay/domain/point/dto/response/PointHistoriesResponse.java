package org.sangyunpark.pointpay.domain.point.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

public class PointHistoriesResponse {
    private long income;
    private long expense;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Builder
    public PointHistoriesResponse(long income, long expense, LocalDateTime startDate, LocalDateTime endDate) {
        this.income = income;
        this.expense = expense;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
