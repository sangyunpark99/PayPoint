package org.sangyunpark.pointpay.domain.point.controller.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

public class PointHistoriesResponse {

    private List<PointHistoryResponse> pointHistories;

    public PointHistoriesResponse(List<PointHistoryResponse> response) {
        this.pointHistories = response;
    }

    public List<PointHistoryResponse> getPointHistories() {
        return this.pointHistories;
    }

    public static class PointHistoryResponse {
        private long income;
        private long expense;
        private LocalDate date;

        @Builder
        public PointHistoryResponse(long income, long expense, LocalDate date) {
            this.income = income;
            this.expense = expense;
            this.date = date;
        }

        public long getIncome() {
            return this.income;
        }

        public long getExpense() {
            return this.expense;
        }

        public LocalDate getDate() {
            return this.date;
        }
    }
}
