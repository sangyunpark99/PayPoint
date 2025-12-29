package org.sangyunpark.pointpay.domain.point.controller.dto.response;

import lombok.Builder;
import org.sangyunpark.pointpay.domain.point.entity.PointType;

import java.time.LocalDateTime;
import java.util.List;

public class PointHistoriesResponse {

    private List<PointHistoryPerMonth> pointHistories;

    public PointHistoriesResponse() {

    }

    public PointHistoriesResponse(List<PointHistoryPerMonth> pointHistories) {
        this.pointHistories = pointHistories;
    }

    public List<PointHistoryPerMonth> getPointHistories() {
        return this.pointHistories;
    }

    public static class PointHistoryPerMonth {
        private long pointBalance;
        private PointType pointType;
        private LocalDateTime createdAt;

        public PointHistoryPerMonth() {

        }

        @Builder
        public PointHistoryPerMonth(long pointBalance, PointType pointType, LocalDateTime createdAt) {
            this.pointBalance = pointBalance;
            this.pointType = pointType;
            this.createdAt = createdAt;
        }

        public long getPointBalance() {
            return this.pointBalance;
        }

        public PointType getPointType() {
            return this.pointType;
        }

        public LocalDateTime getCreatedAt() {
            return this.createdAt;
        }
    }
}
