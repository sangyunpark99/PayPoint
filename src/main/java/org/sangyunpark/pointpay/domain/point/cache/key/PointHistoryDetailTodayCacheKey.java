package org.sangyunpark.pointpay.domain.point.cache.key;

import java.time.LocalDate;
import java.util.Objects;

public class PointHistoryDetailTodayCacheKey {

    private final long userId;
    private final LocalDate createdAt;

    public PointHistoryDetailTodayCacheKey(long userId) {
        this.userId = userId;
        this.createdAt = LocalDate.now();
    }

    public long getUserId() {
        return this.userId;
    }

    public LocalDate getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PointHistoryDetailTodayCacheKey that = (PointHistoryDetailTodayCacheKey) o;
        return userId == that.userId && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, createdAt);
    }
}
