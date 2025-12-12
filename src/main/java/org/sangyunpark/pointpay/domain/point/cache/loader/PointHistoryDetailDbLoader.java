package org.sangyunpark.pointpay.domain.point.cache.loader;

import lombok.RequiredArgsConstructor;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailCacheKey;
import org.sangyunpark.pointpay.domain.point.cache.key.PointHistoryDetailTodayCacheKey;
import org.sangyunpark.pointpay.domain.point.controller.dto.response.PointHistoriesResponse;
import org.sangyunpark.pointpay.domain.point.repository.PointHistoryJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class PointHistoryDetailDbLoader {

    private static final LocalDate today = LocalDate.now();
    private static final LocalDateTime startDateTime = today.atStartOfDay();
    private static final LocalDateTime endDateTime = today.plusDays(1).atStartOfDay();

    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    public PointHistoriesResponse loadFromDbForMonth(PointHistoryDetailCacheKey key) {
        long userId = key.getUserId();
        YearMonth yearMonth = key.getYearMonth();

        LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd;

        YearMonth currentMonth = YearMonth.from(today);

        if(yearMonth.equals(currentMonth)) {
            monthEnd = today.atStartOfDay();
        } else {
            monthEnd = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        }

        return new PointHistoriesResponse(pointHistoryJpaRepository.findAllByUserIdAndCreatedAtBetween(userId, monthStart, monthEnd));
    }

    public PointHistoriesResponse loadFromDbForToday(PointHistoryDetailTodayCacheKey key) {
        long userId = key.getUserId();
        return new PointHistoriesResponse(pointHistoryJpaRepository.findAllByUserIdAndCreatedAtToday(userId, startDateTime, endDateTime));
    }
}
