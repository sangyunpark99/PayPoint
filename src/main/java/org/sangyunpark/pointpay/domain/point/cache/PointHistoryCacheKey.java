package org.sangyunpark.pointpay.domain.point.cache;

import java.time.LocalDateTime;
import java.util.Objects;

public class PointHistoryCacheKey {

    private final long userId;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public PointHistoryCacheKey(long userId, LocalDateTime startDate, LocalDateTime endDate) {
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
        PointHistoryCacheKey that = (PointHistoryCacheKey) o;
        return userId == that.userId && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, startDate, endDate);
    }
}