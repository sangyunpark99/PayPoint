package org.sangyunpark.pointpay.domain.point.cache.key;

import java.time.LocalDateTime;
import java.util.Objects;

public class PointHistorySummaryCacheKey {

    private final long userId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public PointHistorySummaryCacheKey(long userId, LocalDateTime startDate, LocalDateTime endDate) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public long getUserId() {
        return this.userId;
    }

    public LocalDateTime getStartDate() {
        return this.startDate;
    }

    public LocalDateTime getEndDate() {
        return this.endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PointHistorySummaryCacheKey that = (PointHistorySummaryCacheKey) o;
        return userId == that.userId && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, startDate, endDate);
    }
}