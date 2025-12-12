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
        private int pointBalance;
        private PointType pointType;
        private LocalDateTime createdAt;

        public PointHistoryPerMonth() {

        }

        @Builder
        public PointHistoryPerMonth(int pointBalance, PointType type, LocalDateTime createdAt) {
            this.pointBalance = pointBalance;
            this.pointType = type;
            this.createdAt = createdAt;
        }

        public int getPointBalance() {
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
