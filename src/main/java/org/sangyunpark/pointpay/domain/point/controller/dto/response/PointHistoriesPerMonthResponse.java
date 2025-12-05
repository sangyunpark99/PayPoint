package org.sangyunpark.pointpay.domain.point.controller.dto.response;

import lombok.Builder;
import org.sangyunpark.pointpay.domain.point.entity.PointHistory;
import org.sangyunpark.pointpay.domain.point.entity.PointType;

import java.time.LocalDateTime;
import java.util.List;

public class PointHistoriesPerMonthResponse {

    private List<PointHistoryPerMonth> pointHistories;

    public PointHistoriesPerMonthResponse(List<PointHistoryPerMonth> pointHistories) {
        this.pointHistories = pointHistories;
    }

    public List<PointHistoryPerMonth> getPointHistories() {
        return this.pointHistories;
    }

    public static class PointHistoryPerMonth {
        private int pointBalance;
        private PointType type;
        private LocalDateTime createdAt;

        @Builder
        public PointHistoryPerMonth(int pointBalance, PointType type, LocalDateTime createdAt) {
            this.pointBalance = pointBalance;
            this.type = type;
            this.createdAt = createdAt;
        }

        public static PointHistoryPerMonth from(PointHistory pointHistory) {
            return PointHistoryPerMonth.builder()
                    .pointBalance(pointHistory.getPointBalance())
                    .type(pointHistory.getPointType())
                    .createdAt(pointHistory.getCreatedAt())
                    .build();
        }

        public int getPointBalance() {
            return this.pointBalance;
        }

        public PointType getPointType() {
            return this.type;
        }

        public LocalDateTime getCreatedAt() {
            return this.createdAt;
        }
    }
}
