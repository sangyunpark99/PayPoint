package org.sangyunpark.pointpay.domain.point.cache.key;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

public class PointHistoryDetailCacheKey {

    private final long userId;
    private final YearMonth yearMonth;

    public PointHistoryDetailCacheKey(long userId, LocalDateTime startDate) {
        this.userId = userId;
        this.yearMonth = YearMonth.from(startDate);
    }

    public long getUserId() {
        return this.userId;
    }

    public YearMonth getYearMonth() {
        return this.yearMonth;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PointHistoryDetailCacheKey that = (PointHistoryDetailCacheKey) o;
        return userId == that.userId && Objects.equals(yearMonth, that.yearMonth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, yearMonth);
    }
}
